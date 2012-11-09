package com.sappenin.objectify.shardedcounter;


/**
 * A Service class for interacting with ShardedCounters. Capable of
 * incrementing/decrementing the counter.
 * 
 * @author david.fuelling@sappenin.com (David Fuelling)
 */
public interface ShardedCounterService
{

	/**
	 * Increase the number of shards for a given sharded counter. Will never
	 * decrease the number of shards.
	 * 
	 * @param count Number of new shards to build and store
	 * @return The number of shards currently in the Datastore
	 */
	public int addShards(String counterName, int count);

	/**
	 * Retrieve the value of the sharded counter with the specified
	 * {@code counterName}.
	 * 
	 * @param counterName The name of the counter
	 * @return Summed total of all shards' counts
	 */
	public long getCount(String counterName);

	/**
	 * Increment the value of the sharded counter specified by
	 * {@code counterName}.
	 * 
	 * @param counterName
	 * @return Summed total of all shards' counts
	 */
	public void increment(String counterName);

	/**
	 * Reduces the counter specified by {@code counterName} by 1. .
	 * 
	 * @param counterName
	 * @param count
	 * @return Summed total of all shards' counts
	 */
	public void decrement(String counterName);

	/**
	 * Reduces the counter specified by {@code counterName} by the {@code count}
	 * .
	 * 
	 * @param counterName
	 * @param count
	 * @return Summed total of all shards' counts
	 */
	public void decrement(String counterName, int count);

}
