/**
 * Portions of this code are Copyright (c) 2009 and 2011 Google Inc. (see
 * https://developers.google.com/appengine/articles/sharding_counters?hl=ko).
 * Other parts of this code are Copyright (c) 2012 Sappenin Inc. All parts of
 * this code are licensed under the Apache License, Version 2.0.
 * 
 * Copyright (c) 2009 Google Inc.<br/>
 * Copyright (c) 2012 Sappenin Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sappenin.objectify.shardedcounter.service;

import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.sappenin.objectify.shardedcounter.data.Counter;
import com.sappenin.objectify.shardedcounter.data.Counter.CounterStatus;
import com.sappenin.objectify.shardedcounter.data.CounterShard;

/**
 * An implementation of {@link CounterService} that is backed by one or more
 * shards to hold an aggregate counter.<br/>
 * <br/>
 * All datastore operations are performed using Objectify.<br/>
 * <br/>
 * This implementation is capable of incrementing/decrementing various counters
 * but does not reduce the number of shards (in order to maintain high
 * throughput guarantees for when the number of shards has grown). When
 * incrementing, a random shard is selected to prevent a single shard from being
 * written to too frequently. If increments are being made too quickly, this
 * service increases the number of shards in order to handle the per-second load
 * required of it. This implementation assumes an average throughput of 3
 * writes-per-second for a given entity group, and scales shards based on that
 * assumption. For example, if 9 writes per second are noticed, then 3 shards
 * are created and used.<br/>
 * <br/>
 * Lookups are attempted using Memcache. If the counter value is not in the
 * cache, the shards are read from the datastore and accumulated to reconstruct
 * the current count. This operation has a cost of O(numShards), which is
 * dependent on the number of writes/second required.<br/>
 * <br/>
 * In the future, a mechanism to reduce the number of shards may be employed,
 * but this would probably not be necessary since a counter with 100 shards
 * could support approximately 100 increments per-second, which could support at
 * least 360,000 (100c * 60s * 60m) increments per hour, or 8,640,000 increments
 * per day. 100 counters is not very many shards in the grand scheme of things.<br/>
 * <br/>
 * As an upper-bound calculation, the Psy video currently has 750m views in 60
 * days. To get this, we would need approximately 144 shards in order to support
 * 144 updates per second for 60 days.
 * 
 * @author sappenin@gmail.com (David Fuelling)
 */
public class ShardedCounterService implements CounterService
{
	private static final Logger logger = Logger.getLogger(ShardedCounterService.class.getName());

	/**
	 * A random number generating, for distributing writes across shards.
	 */
	private final Random generator = new Random();

	private final MemcacheService memcacheService;
	private final ShardedCounterServiceConfiguration config;

	// /////////////////////////////
	// Constructors
	// /////////////////////////////

	/**
	 * Default Constructor for Dependency-Injection that uses
	 * {@link MemcacheServiceFactory} to populate the memcache service
	 * dependency for this service.
	 */
	public ShardedCounterService()
	{
		this(MemcacheServiceFactory.getMemcacheService());
	}

	/**
	 * Default Constructor for Dependency-Injection that uses a default number
	 * of counter shards (set to 1) and a default configuration per
	 * {@link ShardedCounterServiceConfiguration#defaultConfiguration}.
	 * 
	 * @param memcacheService
	 */
	public ShardedCounterService(final MemcacheService memcacheService)
	{
		this(memcacheService, ShardedCounterServiceConfiguration.defaultConfiguration());
	}

	/**
	 * Default Constructor for Dependency-Injection.
	 * 
	 * @param objectifyFactory An instance of ObjectifyFactory that can be used
	 *            to create a new {@link Objectify}.
	 * @param memcacheService
	 * @param config The configuration for this service
	 */
	public ShardedCounterService(final MemcacheService memcacheService, final ShardedCounterServiceConfiguration config)
	{
		Preconditions.checkNotNull(memcacheService, "Invalid memcacheService!");
		Preconditions.checkNotNull(config);

		this.memcacheService = memcacheService;
		this.config = config;

		if (this.config != null)
		{
			Preconditions.checkArgument(config.getNumInitialShards() > 0,
				"Number of Shards for a new Counter must be greater than 0!");
			if (config.getRelativeUrlPathForDeleteTaskQueue() != null)
			{
				// The relativeUrlPathForDeleteTaskQueue may be null, but if
				// it's non-null, then it must not be blank.
				Preconditions.checkArgument(!StringUtils.isBlank(config.getRelativeUrlPathForDeleteTaskQueue()),
					"Must be null (for the Default Queue) or a non-blank String!");
			}
		}
	}

	// /////////////////////////////
	// Interface Functions
	// /////////////////////////////

	@Override
	public Counter create(final String counterName)
	{
		Preconditions.checkNotNull(counterName);
		Preconditions.checkArgument(!StringUtils.isBlank(counterName));

		// Create a counter with a default num shards, which should be 1 shard
		// to start, offering up to 5 increments per second.

		final Key<Counter> counterKey = new Counter(counterName, 1).getTypedKey();
		final Counter counter = new Counter(counterName, config.getNumInitialShards());
		return ObjectifyService.ofy().transact(new Work<Counter>()
		{
			public Counter run()
			{
				// If a counter already exists, then this 'create' operation
				// should be benign unless the counter is in the "DELETING"
				// phase. In that case, an exception should be
				// thrown
				Counter dsCounter = ObjectifyService.ofy().load().key(counterKey).get();
				if (dsCounter != null && dsCounter.getCounterStatus() == CounterStatus.DELETING)
				{
					// We throw this exception because a particular counter may
					// be in the deleting stage, and we don't want to allow
					// creations to happen when this is occuring.
					throw new RuntimeException("The counter with name \"" + counterName + "\" already exists!");
				}

				ObjectifyService.ofy().save().entity(counter).now();
				return counter;
			}
		});
	}

	// The cache has varying expiration depending on the counter size, so the
	// counter will be accurate after a certain period because this code will
	// perform a re-load from the datastore.
	@Override
	public Optional<Counter> getCounter(String counterName)
	{
		long count = this.getCountFromCacheOrDatastore(counterName);
		Key<Counter> counterKey = new Counter(counterName, 1).getTypedKey();
		// No TX needed - get is Strongly consistent by default
		Counter counter = ObjectifyService.ofy().transactionless().load().key(counterKey).get();
		if (counter != null)
		{
			counter.setApproximateCount(count);
		}
		// Otherwise, return this value...
		return Optional.fromNullable(counter);
	}

	@Override
	public Counter increment(final String counterName, final long amount)
	{
		Preconditions.checkNotNull(counterName);
		Preconditions.checkArgument(!StringUtils.isBlank(counterName));
		Preconditions.checkArgument(amount > 0, "Counter increments must be positive numbers!");

		Optional<Counter> optCounter = this.getCounter(counterName);
		counterPreconditionChecks(counterName, optCounter, "increment");

		// ///////////
		// Increment
		final Long amountIncremented = ObjectifyService.ofy().transact(new Work<Long>()
		{
			@Override
			public Long run()
			{
				CounterShard counterShard = null;

				// Find how many shards are in this counter.
				final int currentNumShards = getShardCount(counterName);

				// Choose the shard randomly from the available shards.
				final int shardNum = generator.nextInt(currentNumShards);

				Optional<CounterShard> optDSCounterShard = getCounterShardFromDS(counterName, shardNum);
				if (optDSCounterShard.isPresent())
				{
					counterShard = optDSCounterShard.get();
				}
				else
				{
					// Lazily create a new CounterShard if one doesn't exist in
					// the Datastore
					counterShard = new CounterShard(counterName, shardNum);
					logger.fine("Creating CounterShard " + shardNum + " for \"" + counterName + "\"");
				}

				counterShard.setCount(counterShard.getCount() + amount);
				logger.fine("Saving CounterShard" + shardNum + " for Counter \"" + counterName + "\" with count "
					+ counterShard.getCount());
				ObjectifyService.ofy().save().entity(counterShard).now();
				return new Long(amount);
			}
		});

		// We use the "amountIncremented" to pause this thread until the TX
		// Future returns. This is because we don't want to increment
		// memcache (below) until the TX has completed. However, the
		// concurrency exception (if any) in the Runnable above won't get
		// thrown until the TX commit is tried, and by that point there's no
		// mechanism to rollback the memcache counter. Using this mechanism,
		// we can guarantee that the thread won't make it here until the TX
		// above commits.

		// /////////////////
		// Increment this counter in memcache atomically
		// /////////////////
		long newAmount = incrementMemcacheAtomic(counterName, amountIncremented.longValue());

		optCounter.get().setApproximateCount(newAmount);
		return optCounter.get();
	}

	/**
	 * Decrement functionality is restricted to 1 in order to provide for
	 * consistency guarantees. For example, if an operation could decrement 10
	 * in a single call, and a particular counter had 10 shards each with 1 as
	 * the count, this would exceed the threshold of Google Appengine
	 * Transaction limits, which are limited to operating on up to 5 entity
	 * groups in a single Transaction.
	 * 
	 * @param counterName
	 * @return
	 */
	@Override
	public Counter decrement(final String counterName)
	{
		Preconditions.checkNotNull(counterName);
		Preconditions.checkArgument(!StringUtils.isBlank(counterName));

		Optional<Counter> optCounter = this.getCounter(counterName);
		counterPreconditionChecks(counterName, optCounter, "decrement");
		if (optCounter.get().getApproximateCount() <= 0)
		{
			logger
				.warning("Attempted to decrement Counter \"" + counterName + "\" but its count was already zero (0)!");
			return optCounter.get();
		}

		// Try a random shard at first -- this will generally work, but if it
		// fails the code below will kick-in, which is slighlty less efficient
		// since it scans through all of the shards and will generally bias the
		// lower-numbered shards.

		long returnablePostDecrementCounterAmount = 0L;

		// Find how many shards are in this counter.
		final int currentNumShards = getShardCount(counterName);
		// Choose the shard randomly from the available shards.
		final int randomShardNum = generator.nextInt(currentNumShards);

		try
		{
			// Try to decrement a random shard. If no exception is thrown, then
			// this function is complete. Return the amount decremented.
			returnablePostDecrementCounterAmount = this.doDecrementInTx(counterName, randomShardNum);
		}
		catch (NonViableDecrementException nvde)
		{
			logger.warning("CounterShard " + randomShardNum + " for CounterName " + counterName
				+ " is a candidate for deletion because it was not able to be decremented!");

			// The random shard above did not have enough in "count" in it to
			// decrement fully, so cycle through all shards to find one to
			// decrement.
			boolean successfulDecrement = false;
			for (int i = 0; i < currentNumShards; i++)
			{
				try
				{
					// Shard numbers start at 0
					returnablePostDecrementCounterAmount = this.doDecrementInTx(counterName, i);
					successfulDecrement = true;
					break;
				}
				catch (NonViableDecrementException nvde2)
				{
					logger.warning("CounterShard " + i + " for CounterName " + counterName
						+ " is a candidate for deletion because it was not able to be decremented!");
					successfulDecrement = false;
					continue;
				}
			}

			if (!successfulDecrement)
			{
				throw new RuntimeException("No suitable shards were found while decrementing counter \"" + counterName
					+ "\" with approximate shard-count of " + currentNumShards);
			}
		}

		// Rely on memcache to return this counter, especially if this counter
		// might be under contention.
		optCounter.get().setApproximateCount(returnablePostDecrementCounterAmount);
		return optCounter.get();
	}

	/**
	 * Helper method for checking the
	 * 
	 * @param counterName
	 * @param optCounter
	 */
	private void counterPreconditionChecks(final String counterName, Optional<Counter> optCounter, String verb)
	{
		if (!optCounter.isPresent())
		{
			throw new RuntimeException("Can't " + verb + " a counter \"" + counterName
				+ "\" that doesn't exist.  Please #create this counter first!");

		}

		if (optCounter.get().getCounterStatus() == CounterStatus.DELETING)
		{
			throw new RuntimeException("Can't " + verb + " counter \"" + counterName
				+ "\" because it is currently being deleted!");
		}
	}

	/**
	 * Attempt to load and decrement a Datastore {@link CounterShard} in a
	 * single transaction.
	 * 
	 * @param counterShardKey
	 * @return The new counter total from memcache after decrementing
	 * @throws NonViableDecrementException If the post-decrement counter update
	 *             was unable to be completed because there was either a
	 *             non-existent shard or the shard didn't have enough count.
	 *             Note that this exception is not thrown if a
	 *             {@link ConcurrentModificationException} is encountered by
	 *             Objectify. In that case, the operation will simply be retried
	 *             until successful.
	 */
	private long doDecrementInTx(final String counterName, final int counterShardNumber)
			throws NonViableDecrementException
	{
		final Key<CounterShard> counterShardKey = new CounterShard(counterName, counterShardNumber).getTypedKey();
		final Long amountDecremented = ObjectifyService.ofy().transact(new Work<Long>()
		{
			@Override
			public Long run()
			{
				Optional<CounterShard> optDSCounterShard = getCounterShardFromDS(counterShardKey);
				if (optDSCounterShard.isPresent())
				{
					if (optDSCounterShard.get().getCount() <= 0)
					{
						throw new NonViableDecrementException(
							"Random Shard \""
								+ counterShardKey.getId()
								+ " \" existed in the Datastore  but its count was already zero.  Aborting decrement for this shard, possibly trying another!");
					}
				}
				else
				{
					throw new NonViableDecrementException(
						"Random Shard \""
							+ counterShardKey.getId()
							+ "\" did not exist in the Datastore.  Aborting decrement for this shard, possibly trying another!");
				}

				CounterShard counterShard = optDSCounterShard.get();
				counterShard.setCount(counterShard.getCount() - 1);
				// Use of now() is required to make the memcache
				// sync code below function properly.
				logger.fine("Saving CounterShard for Decrement with count " + counterShard.getCount());
				ObjectifyService.ofy().save().entity(counterShard).now();

				return new Long(1L);
			}
		});

		// We use the "amountDecremented" to pause this thread until the TX
		// Future returns. This is because we don't want to decrement
		// memcache (below) until the TX has completed. However, the
		// concurrency exception (if any) in the Runnable above won't get
		// thrown until the TX commit is tried, and by that point there's no
		// mechanism to rollback the memcache counter. Using this mechanism,
		// we can guarantee that the thread won't make it here until the TX
		// above commits properly without throwing an Exception

		// Decrement this counter in memcache, but only if un-touched
		long newAmount = incrementMemcacheAtomic(counterName, (amountDecremented * -1));

		// Return the memcache amount because the caller already knows how much
		// the decrement amount was supposed to be
		return newAmount;
	}

	/**
	 * Removes a {@link Counter} from the Datastore and attempts to remove it's
	 * corresponding {@link CounterShard} entities via a Task Queue. This
	 * operation may take some time to complete since it is task queue based, so
	 * constructing or incrementing a Counter while it is being deleted is not
	 * allowed.
	 */
	@Override
	public void delete(final String counterName)
	{
		Preconditions.checkNotNull(counterName);
		Preconditions.checkArgument(!StringUtils.isBlank(counterName));

		// Can't query inside of a TX, so get the counter first
		final Optional<Counter> optCounter = getCounter(counterName);

		if (optCounter.isPresent())
		{
			final Counter counter = optCounter.get();

			// Delete the main counter...
			ObjectifyService.ofy().transact(new VoidWork()
			{
				@Override
				public void vrun()
				{
					Queue queue;
					if (config.getDeleteCounterShardQueueName() == null)
					{
						queue = QueueFactory.getDefaultQueue();
					}
					else
					{
						queue = QueueFactory.getQueue(config.getDeleteCounterShardQueueName());
					}

					// The TaskQueue will delete the counter once all shards are
					// deleted.
					counter.setCounterStatus(CounterStatus.DELETING);
					ObjectifyService.ofy().save().entity(counter).now();

					// Transactionally enqueue this task to the path specified
					// in the constructor (if this is null, then the default
					// queue will be used).
					TaskOptions taskOptions = TaskOptions.Builder.withParam(COUNTER_NAME, counterName);
					if (config.getRelativeUrlPathForDeleteTaskQueue() != null)
					{
						taskOptions = taskOptions.url(config.getRelativeUrlPathForDeleteTaskQueue());
					}

					// Kick off a Task to delete the Shards for this Counter and
					// the Counter itself, but only if the TX succeeds
					queue.add(taskOptions);
				}
			});
		}
	}

	@Override
	public void onTaskQueueCounterDeletion(final String counterName)
	{
		// Instead of the commented code below, get the number of counter shards
		// for the counter and cycle through the shards based upon the counter,
		// and remove them manually. Do this async since we're in a queue and
		// don't need immediate results.

		Optional<Counter> optCounter = this.getCounter(counterName);
		if (!optCounter.isPresent())
		{
			logger.severe("While attempting to delete Counter named \"" + counterName
				+ "\", no Counter was found in the Datastore!");
			return;
		}

		Counter counter = optCounter.get();
		for (int i = 0; i < counter.getNumShards(); i++)
		{
			// Get the Shard and delete it...

			CounterShard tempCounterShard = new CounterShard(counterName, i);
			Key<?> counterShardKey = tempCounterShard.getTypedKey();
			// No TX needed
			ObjectifyService.ofy().transactionless().delete().key(counterShardKey).now();
		}

		// Delete the Counter itself...No TX needed.
		ObjectifyService.ofy().transactionless().delete().key(counter.getTypedKey()).now();
	}

	// //////////////////////////////////
	// Private Helpers
	// //////////////////////////////////

	/**
	 * Increment the memcache version of this counter by one in an atomic
	 * fashion. If another thread increments before this thread, then retry up
	 * to 20 times.
	 * 
	 * @param counterName
	 * @param amount
	 * @return The new count of this counter
	 */
	private long incrementMemcacheAtomic(final String counterName, final long amount)
	{
		// Get the cache counter at a current point in time.
		String memCacheKey = this.assembleCounterKeyforMemcache(counterName);

		int numRetries = 20;
		while (numRetries > 0)
		{
			try
			{
				IdentifiableValue identifiableCounter = memcacheService.getIdentifiable(memCacheKey);
				if (identifiableCounter == null || identifiableCounter.getValue() == null)
				{
					logger
						.severe("No identifiableCounter was found in Memcache.  Unable to Atomically increment for CounterName \""
							+ counterName + "\"");
					break;
				}

				Long cachedCounterAmount = (Long) identifiableCounter.getValue();
				long newAmount = cachedCounterAmount.longValue() + amount;
				if (newAmount < 0)
				{
					newAmount = 0;
				}

				logger.fine("Just before Atomic Incrment of " + amount + ", Memcache has value "
					+ identifiableCounter.getValue());

				if (memcacheService.putIfUntouched(counterName, identifiableCounter, new Long(newAmount)))
				{
					logger.fine("memcacheService.putIfUntouched SUCCESS! with value " + newAmount);
					// If we get here, the put succeeded...
					return newAmount;
				}
				else
				{
					logger.warning("memcacheService.putIfUntouched FAILURE! Retrying...");
				}
			}
			catch (MemcacheServiceException mse)
			{
				// Check and post-decrement the numRetries counter in one step
				if (numRetries-- > 0)
				{
					logger.log(Level.WARNING, "Unable to update memcache counter atomically.  Retrying " + numRetries
						+ " more times!", mse);
				}
				else
				{
					logger.log(Level.SEVERE,
						"Unable to update memcache counter atomically, with no more allowed retries.  Evicting counter named "
							+ counterName + " from the cache!", mse);
					memcacheService.delete(memCacheKey);
					return 0;
				}
			}
		}

		// The increment did not work...
		return 0;
	}

	/**
	 * The cache will expire after 60 seconds, so the counter will be accurate
	 * after a minute because it performs a load from the datastore.
	 * 
	 * @param counterName
	 * @return
	 */
	private long getCountFromCacheOrDatastore(String counterName)
	{
		String memCacheKey = this.assembleCounterKeyforMemcache(counterName);
		Long value = (Long) memcacheService.get(memCacheKey);
		if (value != null)
		{
			// The count was found in memcache, so return it.
			logger.fine("Cache Hit for Counter Named \"" + counterName + "\" returns value: " + value);
			return value;
		}
		else
		{
			// The count was found in memcache, so return it.
			logger.fine("Cache Miss for Counter Named \"" + counterName + "\".  Checking Datastore instead!");
		}

		Key<Counter> counterKey = new Counter(counterName, 1).getTypedKey();
		// No TX needed - get is Strongly consistent by default
		Counter counter = ObjectifyService.ofy().transactionless().load().key(counterKey).get();
		if (counter == null)
		{
			logger.severe("The counter named \"" + counterName
				+ "\" does not exist!  Please create it before trying to get its count!");
			return 0;
		}

		long sum = 0;
		for (int i = 0; i < counter.getNumShards(); i++)
		{
			Key<CounterShard> counterShardKey = new CounterShard(counterName, i).getTypedKey();
			// No TX needed - get is Strongly consistent by default
			CounterShard counterShard = ObjectifyService.ofy().transactionless().load().key(counterShardKey).get();
			if (counterShard != null)
			{
				sum += counterShard.getCount();
			}
		}

		logger.fine("The Datastore is reporting a count of " + sum + " for Counter \"" + counterName
			+ "\" count.  Resetting memcache count to " + sum + " for this counter name");
		memcacheService.put(memCacheKey, new Long(sum), null, SetPolicy.SET_ALWAYS);
		return sum;

	}

	/**
	 * Get the number of shards in the counter specified by {@code counterName}.
	 * 
	 * @param counterName
	 * @return shard count
	 */
	private int getShardCount(String counterName)
	{
		try
		{
			Key<Counter> counterKey = Key.create(Counter.class, counterName);
			// No TX needed - get is Strongly consistent by default, and no
			// other threads increment or decrement this value in this
			// code-base. However, even if future functionality changes make it
			// so that this number is slightly out of date (i.e., the number of
			// CounterShards is actually higher than what this function
			// returns), then this is ok because this thread will use a
			// pre-existing counterShard number (i.e., it's not possible to get
			// a CounterShard number here that would be invalid). Note that this
			// assumption would be invalid if we ever implement code that
			// reduces the number of counter shards (e.g., via a composition job
			// that reduces the shards and combines shard-counts into a single
			// shard -- if, say, the traffic on this counter were to go way down
			// and many shards aren't needed).
			Counter counter = ObjectifyService.ofy().transactionless().load().key(counterKey).get();
			if (counter != null)
			{
				int shardCount = counter.getNumShards();
				return shardCount;
			}
			else
			{
				return config.getNumInitialShards();
			}
		}
		catch (RuntimeException re)
		{
			return config.getNumInitialShards();
		}
	}

	/**
	 * Helper function to get a named {@link Counter} from the datastore.
	 * 
	 * @param counterName
	 * @param shardNumber
	 * @return
	 */
	private Optional<CounterShard> getCounterShardFromDS(String counterName, int shardNumber)
	{
		Key<CounterShard> counterShardKey = new CounterShard(counterName, shardNumber).getTypedKey();
		return this.getCounterShardFromDS(counterShardKey);
	}

	/**
	 * Helper function to get a named {@link Counter} from the Datastore.
	 * 
	 * @param counterName
	 * @param shardNumber
	 * @return
	 */
	private Optional<CounterShard> getCounterShardFromDS(Key<CounterShard> counterShardKey)
	{
		CounterShard counterShard = null;
		try
		{
			// Transactional Get is required here because the TX doesn't
			// actually start until the Datastore is hit. Thus, it's possible
			// for two threads to get the same counterShard and increment it
			// once yielding an invalid count unless this load is done in the
			// existing transaction, if any (Without a TX here, a second tx
			// commit would overwrite the first).
			counterShard = ObjectifyService.ofy().load().key(counterShardKey).get();
		}
		catch (RuntimeException re)
		{
			// ... Do nothing here. An EntityNotFoundExecption can be thrown
			// from Objectify if nothing exists in the DS
		}

		return Optional.fromNullable(counterShard);
	}

	/**
	 * Assembles a CounterKey for Memcache
	 * 
	 * @param counterName
	 * @return
	 */
	private String assembleCounterKeyforMemcache(String counterName)
	{
		return counterName;
	}

	/**
	 * Internal unchecked exception thrown when a particular counter shard is
	 * unable to decrement because its count is already zero. This exception is
	 * unchecked because to operate properly inside of the Objectify
	 * {@link Work} interface.
	 * 
	 * @author David Fuelling <sappenin@gmail.com>
	 * 
	 */
	private static final class NonViableDecrementException extends RuntimeException
	{
		private static final long serialVersionUID = -2578731596507267699L;

		public NonViableDecrementException(String message)
		{
			super(message);
		}
	}

}
