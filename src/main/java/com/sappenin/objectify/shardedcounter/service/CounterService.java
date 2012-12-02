/**
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

import com.google.common.base.Optional;
import com.sappenin.objectify.shardedcounter.data.Counter;

/**
 * A service for storing, deleting, and interacting with Counters.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public interface CounterService
{
	public static final String COUNTER_NAME = "counterName";

	/**
	 * Create a new Counter with a default number of shards. If the counter
	 * already exists, then this function will throw a runtime exception.
	 * 
	 * @param counterName
	 * @return
	 */
	public Counter create(final String counterName);

	/**
	 * Async-delete a counter and all of its shards
	 * 
	 * @param counterName
	 */
	public void delete(final String counterName);

	/**
	 * Retrieve the value of the counter with the specified {@code counterName}.
	 * 
	 * @param counterName
	 * @return An Optional Counter with the summed-total of its shards' counts,
	 *         if available
	 */
	public Optional<Counter> getCounter(final String counterName);

	/**
	 * Increment the value of the sharded counter specified by
	 * {@code counterName}. Perform a gerCount to get the count.
	 * 
	 * @param counterName
	 * @param amount The amount to increment by (positive or negative)
	 * @return An Optional Counter with the new count
	 */
	public Optional<Counter> increment(final String counterName, final long amount);

	/**
	 * Decrement the value of the sharded counter with name {@code counterName}
	 * by 1.
	 * 
	 * @param counterName
	 * @return An Optional Counter with the new count
	 */
	public Optional<Counter> decrement(final String counterName);

	/**
	 * Provided here for convenience as a callback method that a task queue
	 * should call in order to remove counter shards for a particular counter.
	 * 
	 * @param counterName
	 */
	public void onTaskQueueCounterDeletion(String counterName);

}
