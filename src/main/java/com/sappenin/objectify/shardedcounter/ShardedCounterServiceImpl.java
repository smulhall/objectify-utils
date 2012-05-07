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

	// The number of shards to begin with for this counter
	private static final int DEFAULT_NUM_COUNTER_SHARDS = 3;

	// Prefix for storing counter shards in memcache
	// TODO Make this configurable
	private static final String MEMCACHE_KEY__COUNTER__PREFIX = "CounterShard_";

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
	public int addShards(String counterName, int count)
	{
		Counter counter = this.getCounter(counterName);

		if (counter == null)
		{
			counter = new Counter(counterName, this.numInitialShards);
		}
		// We don't init the CounterShards here - they are lazily init'd on each
		// increment
		counter.setNumShards(counter.getNumShards() + count);
		this.saveInTransactionAndCommit(counter);

		return counter.getNumShards();
	}

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
	public void increment(String counterName)
	{
		// Find how many shards are in this counter.
		int numShards = getShardCount(counterName);

		// Choose the shard randomly from the available shards.
		long shardNum = generator.nextInt(numShards);

		// Load the Shard from the Datastore
		final CounterShard counterShard = this.getCounterShard(counterName, shardNum);
		counterShard.setCount(counterShard.getCount() + 1);

		this.saveInTransactionAndCommit(counterShard);

		// Increment this counter in memcache
		String memCacheKey = this.assembleCounterKeyforMemcache(counterName);

		memcacheService.increment(memCacheKey, 1);
	}

	private <T extends Object> void saveInTransactionAndCommit(T entity)
	{
		Objectify ofyTrans = this.ofy().transaction();
		ofyTrans.save().entity(entity).now();
		if (ofyTrans.getTxn() != null && ofyTrans.getTxn().isActive())
		{
			ofyTrans.getTxn().commit();
		}
	}

	@Override
	public void decrement(String counterName)
	{
		decrement(counterName, 1);
	}

	/**
	 * Decrements evenly across all counters
	 */
	@Override
	public void decrement(String counterName, int count)
	{
		// Find how many shards are in this counter.
		int numShards = getShardCount(counterName);

		for (int i = 0; i < count; i++)
		{
			// Choose the shard randomly from the available shards.
			long shardNum = generator.nextInt(numShards);

			// Load the Shard from the Datastore
			CounterShard counterShard = this.getCounterShard(counterName, shardNum);
			if (counterShard == null)
			{
				// Do nothing and return. We can't decrement a counter to be
				// less
				// than 1.
			}
			else
			{
				// Ensure that this counter doesn't go negative
				if (counterShard.getCount() < 1)
				{
					counterShard.setCount(0);
				}
				else
				{
					counterShard.setCount(counterShard.getCount() - 1);
				}

				this.saveInTransactionAndCommit(counterShard);
				memcacheService.increment(this.assembleCounterKeyforMemcache(counterName), -1);
			}
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
			Counter counter = this.getCounter(counterName);
			int shardCount = counter.getNumShards();
			return shardCount;
		}
		catch (RuntimeException re)
		{
			return DEFAULT_NUM_COUNTER_SHARDS;
		}
	}

	/** Work which doesn't require returning a value */
	public static abstract class VoidWork implements TxnWork<Objectify, Void>
	{
		@Override
		public Void run(Objectify ofy)
		{
			this.vrun(ofy);
			return null;
		}

		abstract public void vrun(Objectify ofy);
	}

	private Objectify ofy()
	{
		return this.objectifyFactory.begin();
	}

	/**
	 * Helper function to get a named {@link Counter} from the datastore.
	 * 
	 * @return
	 */
	private Counter getCounter(String counterName)
	{
		Counter counter = null;
		try
		{
			com.googlecode.objectify.Key<Counter> counterKey = this.assembleCounterKey(counterName);
			counter = this.ofy().load().type(Counter.class).filterKey(counterKey).first().get();

			if (counter == null)
			{
				// Initialize the counter in the DS
				counter = new Counter(counterName, DEFAULT_NUM_COUNTER_SHARDS);
				this.saveInTransactionAndCommit(counter);
			}
		}
		catch (RuntimeException re)
		{
			// ... Do nothing here. An EntityNotFoundExecption can be thrown
			// from Objectify if nothing exists in the DS
		}

		return counter;
	}

	/**
	 * Helper function to get a named {@link Counter} from the datastore.
	 * 
	 * @param counterName
	 * @param shardNumber
	 * @return
	 */
	private CounterShard getCounterShard(String counterName, long shardNumber)
	{
		CounterShard counterShard = null;
		try
		{
			com.googlecode.objectify.Key<CounterShard> counterShardKey = this.assembleCounterShardKey(counterName,
				shardNumber);
			counterShard = this.ofy().load().type(CounterShard.class).filterKey(counterShardKey).first().get();
		}
		catch (RuntimeException re)
		{
			// ... Do nothing here. An EntityNotFoundExecption can be thrown
			// from Objectify if nothing exists in the DS
		}

		// Create a new CounterShard if none is found in the DB.
		if (counterShard == null)
		{
			counterShard = new CounterShard(this.assembleCounterShardIdentifier(counterName, shardNumber));
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
	 * Assembles a Key<Counter> based upon the specified {@code counterName}.
	 * 
	 * @param counterName
	 * @param counterShardNumber
	 * @return
	 */
	private com.googlecode.objectify.Key<CounterShard> assembleCounterShardKey(String counterName,
			long counterShardNumber)
	{
		String counterShardIdentifier = this.assembleCounterShardIdentifier(counterName, counterShardNumber);
		com.googlecode.objectify.Key<CounterShard> counterKey = com.googlecode.objectify.Key.create(CounterShard.class,
			counterShardIdentifier);
		return counterKey;
	}

	/**
	 * Assembles the unique identifier for a {@link CounterShard}.
	 * 
	 * @param counterName
	 * @param counterShardNumber
	 * @return
	 */
	private String assembleCounterShardIdentifier(String counterName, long counterShardNumber)
	{
		return counterName + "_" + counterShardNumber;
	}

	private String assembleCounterKeyforMemcache(String counterName)
	{
		return MEMCACHE_KEY__COUNTER__PREFIX + counterName;
	}

}
