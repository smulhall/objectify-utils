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

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.Preconditions;

/**
 * A Configuration class for {@link ShardedCounterService}.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
@Getter
@ToString
@EqualsAndHashCode
@Immutable
public class ShardedCounterServiceConfiguration
{
	// The number of shards to begin with for this counter.
	static final int DEFAULT_NUM_COUNTER_SHARDS = 1;

	// The number of counter shards to create when a new counter is created. The
	// default value is 1.
	private final int numInitialShards;

	// The name of the queue that will be used to delete shards in an async
	// fashion
	private final String deleteCounterShardQueueName;

	// The optional value of {@link TaskBuilder#url} when interacting with the
	// queue used to delete CounterShards.
	private final String relativeUrlPathForDeleteTaskQueue;

	/**
	 * The default constructor for building a ShardedCounterService
	 * configuration class. Private so that only the builder can build this
	 * class.
	 * 
	 * @param builder
	 */
	private ShardedCounterServiceConfiguration(Builder builder)
	{
		Preconditions.checkNotNull(builder);
		this.numInitialShards = builder.numInitialShards;
		this.deleteCounterShardQueueName = builder.deleteCounterShardQueueName;
		this.relativeUrlPathForDeleteTaskQueue = builder.relativeUrlPathForDeleteTaskQueue;
	}

	/**
	 * Constructs a {@link ShardedCounterServiceConfiguration} object with
	 * default values.
	 * 
	 * @return
	 */
	public static ShardedCounterServiceConfiguration defaultConfiguration()
	{
		ShardedCounterServiceConfiguration.Builder builder = new ShardedCounterServiceConfiguration.Builder();
		return new ShardedCounterServiceConfiguration(builder);
	}

	/**
	 * A Builder for {@link ShardedCounterServiceConfiguration}.
	 * 
	 * @author david
	 */
	public static final class Builder
	{
		@Getter
		@Setter
		private int numInitialShards;

		@Getter
		@Setter
		private String deleteCounterShardQueueName;

		@Getter
		@Setter
		private String relativeUrlPathForDeleteTaskQueue;

		/**
		 * Default Constructor. Sets up this buildr with 1 shard by default.
		 */
		public Builder()
		{
			this.numInitialShards = DEFAULT_NUM_COUNTER_SHARDS;
		}

		public Builder withNumInitialShards(int numInitialShards)
		{
			Preconditions.checkArgument(numInitialShards > 0,
				"Number of Shards for a new Counter must be greater than 0!");
			this.numInitialShards = numInitialShards;
			return this;
		}

		public Builder withDeleteCounterShardQueueName(String deleteCounterShardQueueName)
		{
			this.deleteCounterShardQueueName = deleteCounterShardQueueName;
			return this;
		}

		public Builder withRelativeUrlPathForDeleteTaskQueue(String relativeUrlPathForDeleteTaskQueue)
		{
			this.relativeUrlPathForDeleteTaskQueue = relativeUrlPathForDeleteTaskQueue;
			return this;
		}

		/**
		 * Method to build a new {@link ShardedCounterServiceConfiguration}.
		 * 
		 * @return
		 */
		public ShardedCounterServiceConfiguration build()
		{
			return new ShardedCounterServiceConfiguration(this);
		}

	}

}
