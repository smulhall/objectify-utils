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
package com.sappenin.objectify.shardedcounter.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 * Represents a counter in the datastore and stores the number of shards.
 * 
 * @author david.fuelling@sappenin.com (David Fuelling)
 */
@Entity
@Unindex
public class Counter
{
	@Id
	private String counterName;
	// This is necessary to know in order to be able to evenly distribute
	// amongst all shards for a given counterName
	private int numShards;

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
		this.counterName = counterName;
		this.numShards = numShards;
	}

	// //////////////////////////////
	// Getters/Setters
	// //////////////////////////////

	/**
	 * @return the counterName
	 */
	public String getCounterName()
	{
		return counterName;
	}

	/**
	 * @param counterName the counterName to set
	 */
	public void setCounterName(String counterName)
	{
		this.counterName = counterName;
	}

	/**
	 * @return the numShards
	 */
	public int getNumShards()
	{
		return numShards;
	}

	/**
	 * @param numShards the numShards to set
	 */
	public void setNumShards(int numShards)
	{
		this.numShards = numShards;
	}

}
