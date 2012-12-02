package com.sappenin.objectify.shardedcounter.service;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.Preconditions;

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
	 * configuration class.
	 * 
	 * @param builder
	 */
	public ShardedCounterServiceConfiguration(Builder builder)
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

	}

}
