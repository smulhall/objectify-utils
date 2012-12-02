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

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Unindex;
import com.sappenin.objectify.shardedcounter.data.base.AbstractEntity;

/**
 * Represents a counter in the datastore and stores the number of shards.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
@Entity
@Getter
@Setter
@Unindex
public class Counter extends AbstractEntity
{

	// Used by the Get methods to indicate the state of a Counter while it is
	// deleting.
	public static enum CounterStatus
	{
		AVAILABLE, DELETING; // INCREMENTING, DECREMENTING?
	};

	// The counter name is the @Id of this entity, found in AbstractEntity

	// This is an approximateCount, as aggregated from the CounterShards. It is
	// ignored because it is used as a read-only value. NEVER
	// SET THIS VALUE EXTERNALLY AND TRY TO SAVE IT TO THE DATASTORE. CALL
	// COUNTERSERVICE#INCREMENT OR DECREMENT INSTEAD.
	@com.googlecode.objectify.annotation.Ignore
	private long approximateCount;

	// This is necessary to know in order to be able to evenly distribute
	// amongst all shards for a given counterName
	private int numShards;

	// This is AVAILABLE by default, which means it can be incremented and
	// decremented
	private CounterStatus counterStatus = CounterStatus.AVAILABLE;

	/**
	 * Default Constructor for Objectify
	 * 
	 * @deprecated Use the param-based constructors instead.
	 */
	@Deprecated
	public Counter()
	{
		// Implement for Objectify
	}

	/**
	 * The param-based constructor
	 * 
	 * @param counterName
	 * @param numShards
	 */
	public Counter(String counterName, int numShards)
	{
		super(counterName);
		this.numShards = numShards;
	}

	// //////////////////////////////
	// Getters/Setters
	// //////////////////////////////

	/**
	 * @return The name of this counter
	 */
	public String getCounterName()
	{
		return this.getId();
	}

	public void setNumShards(int numShards)
	{
		this.numShards = numShards;
		this.setUpdatedDateTime(new DateTime(DateTimeZone.UTC));
	}

}
