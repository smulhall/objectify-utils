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
package com.sappenin.objectify.shardedcounter.data;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Preconditions;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Unindex;
import com.sappenin.objectify.shardedcounter.data.base.AbstractEntity;

/**
 * Represents a discrete shard belonging to a named counter.<br/>
 * <br/>
 * An individual shard is written to infrequently to allow the counter in
 * aggregate to be incremented rapidly.
 * 
 * @author david.fuelling@sappenin.com (David Fuelling)
 */
@Entity
@Getter
@Setter
@Unindex
public class CounterShard extends AbstractEntity
{
	static final String COUNTER_SHARD_KEY_SEPARATOR = "-";

	// The id of a CounterShard is the name of the counter (for easy lookup via
	// a starts-with query) combined with the shardNumber. E.g., "CounterName-2"
	// would be the counter with name "CounterName" and Shard number 2.

	// The total of this shard's counter (not the total counter count)
	private long count;

	/**
	 * Default Constructor for Objectify
	 * 
	 * @deprecated Use the param-based constructors instead.
	 */
	@Deprecated
	public CounterShard()
	{
		// Implemented for Objectify
	}

	/**
	 * Param-based Constructor
	 * 
	 * @param counterName
	 * @param shardNumber
	 */
	public CounterShard(final String counterName, final int shardNumber)
	{
		Preconditions.checkNotNull(counterName);
		setId(counterName + COUNTER_SHARD_KEY_SEPARATOR + shardNumber);
	}

	// /////////////////////////
	// Getters/Setters
	// /////////////////////////

	/**
	 * @return The last dateTime that an increment occurred.
	 */
	public DateTime getLastIncrement()
	{
		return getUpdatedDateTime();
	}

	/**
	 * Set the amount of this shard with a new {@code count}.
	 * 
	 * @param count
	 */
	public void setCount(long count)
	{
		this.count = count;
		this.setUpdatedDateTime(DateTime.now(DateTimeZone.UTC));
	}

}
