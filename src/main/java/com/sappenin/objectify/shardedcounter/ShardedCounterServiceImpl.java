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
package com.sappenin.objectify.shardedcounter;

import java.util.Random;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.TxnWork;
import com.sappenin.objectify.shardedcounter.model.Counter;
import com.sappenin.objectify.shardedcounter.model.CounterShard;

/**
 * A Service class for interacting with ShardedCounters using Objectify.
 * 
 * Capable of incrementing/decrementing the counter but not capable of reducing
 * the number of shards. When incrementing, a random shard is selected to
 * prevent a single shard from being written to too frequently. If increments
 * are being made too quickly, increase the number of shards to divide the load.
 * Performs datastore operations using Objectify.
 * 
 * Lookups are attempted using Memcache. If the counter value is not in the
 * cache, the shards are read from the datastore and accumulated to reconstruct
 * the current count.
 * 
 * @author david.fuelling@sappenin.com (David Fuelling)
 */
public class ShardedCounterServiceImpl implements ShardedCounterService
{
	private static final String LARGEST_STRING = "\ufffd";

	// The number of shards to begin with for this counter.
	// TODO Make this configurable
	private static final int DEFAULT_NUM_COUNTER_SHARDS = 3;

	/**
	 * A random number generating, for distributing writes across shards.
	 */
	private final Random generator = new Random();

	private final ObjectifyFactory objectifyFactory;
	private final MemcacheService memcacheService;
	private final int numInitialShards;

	// /////////////////////////////
	// Constructors
	// /////////////////////////////

	/**
	 * Default Constructor for Dependency-Injection.
	 * 
	 * @param objectifyFactory An instance of ObjectifyFactory that can be used
	 *            to create a new {@link Objectify}.
	 * @param memcacheService
	 * @param numInitialShards The number of initial {@link CounterShard}
	 *            objects to create for a new counter.
	 */
	public ShardedCounterServiceImpl(final ObjectifyFactory objectifyFactory, final MemcacheService memcacheService)
	{
		this(objectifyFactory, memcacheService, DEFAULT_NUM_COUNTER_SHARDS);
	}

	/**
	 * Default Constructor for Dependency-Injection.
	 * 
	 * @param objectifyFactory An instance of ObjectifyFactory that can be used
	 *            to create a new {@link Objectify}.
	 * @param memcacheService
	 * @param numInitialShards The number of initial {@link CounterShard}
	 *            objects to create for a new counter.
	 */
	public ShardedCounterServiceImpl(final ObjectifyFactory objectifyFactory, final MemcacheService memcacheService,
			final int numInitialShards)
	{
		this.objectifyFactory = objectifyFactory;
		this.memcacheService = memcacheService;
		this.numInitialShards = numInitialShards;
	}

	// /////////////////////////////
	// Utility Functions
	// /////////////////////////////

	@Override
	public int addShards(final String counterName, final int count)
	{
		// We don't init the CounterShards here - they are lazily init'd on each
		// increment
		Integer INumShards = ofy().transact(0, new TxnWork<Objectify, Integer>()
		{
			@Override
			public Integer run(Objectify ofy)
			{
				Counter counter = getCounter(counterName, ofy);
				if (counter == null)
				{
					counter = new Counter(counterName, numInitialShards);
				}
				counter.setNumShards(counter.getNumShards() + count);
				ofy.save().entity(counter).now();
				return counter.getNumShards();
			}
		});

		return INumShards == null ? 0 : INumShards.intValue();
	}

	// The cache will expire after 60 seconds, so the counter will be accurate
	// after a minute because it performs a load from the datastore.
	@Override
	public long getCount(String counterName)
	{
		String memCacheKey = this.assembleCounterKeyforMemcache(counterName);

		Long value = (Long) memcacheService.get(memCacheKey);
		if (value != null)
		{
			return value;
		}

		Key counterShardKeyStartsWith = KeyFactory.createKey(CounterShard.class.getSimpleName(), counterName);
		Key counterShardKeyEndsWith = KeyFactory.createKey(CounterShard.class.getSimpleName(), counterName + "_"
			+ LARGEST_STRING);

		// List<CounterShard> shards =
		// this.ofy().load().type(CounterShard.class).list();
		// for (CounterShard shard : shards)
		// {
		// System.out.println(shard.getCounterName());
		// }

		// Get all CounterShards that "start with" 'counterName'.
		QueryResultIterator<CounterShard> iterator = this.ofy().load().type(CounterShard.class)
			.filter("__key__ >=", counterShardKeyStartsWith).filter("__key__ <", counterShardKeyEndsWith).iterator();

		long sum = 0;
		while (iterator.hasNext())
		{
			sum += iterator.next().getCount();
		}

		memcacheService.put(memCacheKey, new Long(sum), Expiration.byDeltaSeconds(60),
			SetPolicy.ADD_ONLY_IF_NOT_PRESENT);

		return sum;
	}

	@Override
	public void increment(final String counterName)
	{
		this.increment(counterName, this.ofy());
	}

	@Override
	public void increment(final String counterName, Objectify ofy)
	{
		// Find how many shards are in this counter.
		int numShards = getShardCount(counterName);

		// Choose the shard randomly from the available shards.
		final long shardNum = generator.nextInt(numShards);

		// /////////
		// In a TX: Load the Shard from the Datastore, Increment it, and save it
		// back

		ofy().transact(0, new TxnWork<Objectify, CounterShard>()
		{
			@Override
			public CounterShard run(final Objectify ofy)
			{
				final CounterShard counterShard = getCounterShard(counterName, shardNum, ofy);
				counterShard.setCount(counterShard.getCount() + 1);
				ofy.save().entity(counterShard).now();
				return counterShard;
			}
		});

		// Increment this counter in memcache
		String memCacheKey = this.assembleCounterKeyforMemcache(counterName);

		memcacheService.increment(memCacheKey, 1);
	}

	@Override
	public void decrement(String counterName)
	{
		decrement(counterName, 1);
	}

	@Override
	public void decrement(String counterName, Objectify ofy)
	{
		decrement(counterName, 1, ofy);
	}

	@Override
	public void decrement(final String counterName, final int count)
	{
		this.decrement(counterName, count, this.ofy());
	}

	@Override
	public void decrement(final String counterName, int count, Objectify ofy)
	{
		if (count < 1)
		{
			// Don't waste this counter's time on an invalid decrement amount.
			return;
		}

		// Find how many shards are in this counter.
		final int numShards = getShardCount(counterName);

		for (int i = 0; i < count; i++)
		{
			// Choose the shard randomly from the available shards.
			final long shardNum = generator.nextInt(numShards);

			ofy.transact(0, new TxnWork<Objectify, CounterShard>()
			{
				@Override
				public CounterShard run(final Objectify ofy)
				{
					final CounterShard counterShard = getCounterShard(counterName, shardNum, ofy);
					if (counterShard == null)
					{
						// Do nothing and return. We can't decrement a counter
						// to be less than 1.
					}
					else
					{
						if (counterShard.getCount() == 0)
						{
							// Do nothing here. The count should not go below
							// zero, and it's a waste of resources to re-save
							// the CounterShard since nothing has changed.
							return counterShard;
						}
						else if (counterShard.getCount() <= 1)
						{
							// Ensure that this counter doesn't go negative, but
							// change the value to 0 (this block captures any
							// negative values, or the value "1").
							counterShard.setCount(0);
						}
						else
						{
							// Otherwise, decrement the counter and save the
							// value
							counterShard.setCount(counterShard.getCount() - 1);
						}

						ofy.save().entity(counterShard).now();

					}
					return counterShard;
				}
			});

			// Decrement memcache
			memcacheService.increment(this.assembleCounterKeyforMemcache(counterName), -1);
		}
	}

	// //////////////////////////////////
	// Private Helpers
	// //////////////////////////////////

	/**
	 * Get the number of shards in the counter specified by {@code counterName}.
	 * 
	 * @param counterName
	 * @return shard count
	 */
	protected int getShardCount(String counterName)
	{
		try
		{
			Counter counter = this.getCounter(counterName, this.ofy());
			int shardCount = counter.getNumShards();
			return shardCount;
		}
		catch (RuntimeException re)
		{
			return DEFAULT_NUM_COUNTER_SHARDS;
		}
	}

	/**
	 * 
	 * @return
	 */
	private Objectify ofy()
	{
		return this.objectifyFactory.begin();
	}

	/**
	 * Helper function to get a named {@link Counter} from the datastore.
	 * 
	 * @return
	 */
	private Counter getCounter(final String counterName, Objectify ofyTx)
	{
		Counter returnableCounter = null;

		try
		{
			com.googlecode.objectify.Key<Counter> counterKey = this.assembleCounterKey(counterName);
			returnableCounter = ofyTx.load().type(Counter.class).filterKey(counterKey).first().get();
		}
		catch (RuntimeException re)
		{
			// ... Do nothing here. An EntityNotFoundExecption can be thrown
			// from Objectify if nothing exists in the DS, in which case the
			// block below will be executed.
		}

		// If no counter is found in the DS, create one and return it.
		if (returnableCounter == null)
		{
			// Initialize the counter in the DS
			returnableCounter = ofyTx.transact(0, new TxnWork<Objectify, Counter>()
			{
				@Override
				public Counter run(Objectify ofy)
				{
					final Counter counter = new Counter(counterName, DEFAULT_NUM_COUNTER_SHARDS);
					ofy.save().entity(counter).now();
					return counter;
				}
			});
		}

		return returnableCounter;
	}

	/**
	 * Helper function to get a named {@link Counter} from the datastore.
	 * 
	 * @param counterName
	 * @param shardNumber
	 * @return
	 */
	private CounterShard getCounterShard(String counterName, long shardNumber, Objectify ofy)
	{
		CounterShard counterShard = null;

		String counterShardKeyIdentifier = this.assembleCounterShardKeyIdentifier(counterName, shardNumber);

		try
		{
			counterShard = ofy.load().type(CounterShard.class).id(counterShardKeyIdentifier).get();
		}
		catch (RuntimeException re)
		{
			// ... Do nothing here. An EntityNotFoundExecption can be thrown
			// from Objectify if nothing exists in the DS
		}

		// Create a new CounterShard if none is found in the DB.
		if (counterShard == null)
		{
			counterShard = new CounterShard(counterShardKeyIdentifier);
		}

		return counterShard;
	}

	/**
	 * Assembles a Key<Counter> based upon the specified {@code counterName}.
	 * 
	 * @param counterName
	 * @return
	 */
	private com.googlecode.objectify.Key<Counter> assembleCounterKey(String counterName)
	{
		com.googlecode.objectify.Key<Counter> counterKey = com.googlecode.objectify.Key.create(Counter.class,
			counterName);
		return counterKey;
	}

	/**
	 * Assembles the unique identifier for a {@link CounterShard}.
	 * 
	 * @param counterName
	 * @param counterShardNumber
	 * @return
	 */
	private String assembleCounterShardKeyIdentifier(String counterName, long counterShardNumber)
	{
		return counterName + "_" + counterShardNumber;
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

}
