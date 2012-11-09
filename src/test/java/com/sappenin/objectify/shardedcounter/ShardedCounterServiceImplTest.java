/**
 * This code is Copyright (c) 2012 Sappenin Inc.All parts of this code are
 * licensed under the Apache License, Version 2.0.
 * 
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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sappenin.objectify.BaseObjectifyTest;
import com.sappenin.objectify.shardedcounter.model.Counter;
import com.sappenin.objectify.shardedcounter.model.CounterShard;

/**
 * Test class for {@link ShardedCounterServiceImpl}.
 * 
 * @author david.fuelling@sappenin.com (David Fuelling)
 */
public class ShardedCounterServiceImplTest extends BaseObjectifyTest
{
	private static final String TEST_COUNTER1 = "test-counter1";
	private static final String TEST_COUNTER2 = "test-counter2";

	ShardedCounterService shardedCounterService;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		fact.register(Counter.class);
		fact.register(CounterShard.class);
	}

	@After
	public void tearDown()
	{
		super.tearDown();
	}

	// /////////////////////////
	// Unit Tests
	// /////////////////////////

	@Test
	public void testShardedCounterServiceConstructor()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache);
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER1));
	}

	@Test
	public void testShardedCounterServiceConstructorFull()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 10);
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER1));
	}

	@Test
	public void testIncrement_DefaultNumShards()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache);
		doCounterAssertions();
	}

	@Test
	public void testIncrement_Specifiy10Shards()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 10);
		doCounterAssertions();
	}

	@Test
	public void testIncrement_Specifiy1Shard()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 1);
		doCounterAssertions();
	}

	@Test
	public void testGetCount()
	{
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(3, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(4, shardedCounterService.getCount(TEST_COUNTER2));
	}

	@Test
	public void testAddShards_AddToFirstCounter()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(3, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(4, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

		shardedCounterService.addShards(TEST_COUNTER1, 1);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(6, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(8, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(4, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

	}

	@Test
	public void testAddShards_AddToSecondCounter()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(3, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(4, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

		shardedCounterService.addShards(TEST_COUNTER2, 1);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(6, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(8, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(4, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

	}

	@Test
	public void testAddShards_AddToBoth()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(3, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(4, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(3, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

		shardedCounterService.addShards(TEST_COUNTER1, 1);
		shardedCounterService.addShards(TEST_COUNTER2, 1);

		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER2);
		shardedCounterService.increment(TEST_COUNTER1);
		shardedCounterService.increment(TEST_COUNTER2);

		assertEquals(6, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(8, shardedCounterService.getCount(TEST_COUNTER2));

		assertEquals(4, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER1));
		assertEquals(4, ((ShardedCounterServiceImpl) shardedCounterService).getShardCount(TEST_COUNTER2));

	}

	@Test
	public void testDecrementString_OneCounter()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		this.doCounterAssertions();

		// Decrement 5
		this.shardedCounterService.decrement(TEST_COUNTER1, 5);

		assertEquals(5, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER2));

	}

	@Test
	public void testDecrementString_TwoCounters()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		this.doCounterAssertions();

		// Decrement 4

		this.shardedCounterService.decrement(TEST_COUNTER1, 4);
		assertEquals(6, shardedCounterService.getCount(TEST_COUNTER1));
		this.shardedCounterService.decrement(TEST_COUNTER2);
		assertEquals(6, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(5, shardedCounterService.getCount(TEST_COUNTER2), 5);
	}

	@Test
	public void testDecrementAll()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		this.doCounterAssertions();

		// Decrement 10
		this.shardedCounterService.decrement(TEST_COUNTER1, 10);
		this.shardedCounterService.decrement(TEST_COUNTER2, 10);

		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER2));
	}

	@Test
	public void testDecrementNegative()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		this.doCounterAssertions();

		// Decrement 20

		this.shardedCounterService.decrement(TEST_COUNTER1, 20);
		this.shardedCounterService.decrement(TEST_COUNTER2, 20);

		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER2));
	}

	@Test
	public void testDecrementNegative_Iteratively()
	{
		// Use 3 shards
		shardedCounterService = new ShardedCounterServiceImpl(this.memcache, 3);

		this.doCounterAssertions();

		// Decrement 20

		for (int i = 0; i < 20; i++)
		{
			this.shardedCounterService.decrement(TEST_COUNTER1);
			this.shardedCounterService.decrement(TEST_COUNTER2);
		}

		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER1));
		assertEquals(0, shardedCounterService.getCount(TEST_COUNTER2));
	}

	// /////////////////////////
	// Private Helpers
	// /////////////////////////

	private void doCounterAssertions()
	{
		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(1L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(2L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(3L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(4L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(5L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(6L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(7L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(8L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(9L, shardedCounterService.getCount(TEST_COUNTER1));

		shardedCounterService.increment(TEST_COUNTER1);
		assertEquals(10L, shardedCounterService.getCount(TEST_COUNTER1));
	}

}
