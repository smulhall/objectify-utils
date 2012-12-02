/**
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
package com.googlecode.objectify.test.util;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cache.AsyncCacheFilter;

/**
 * All tests should extend this class to set up the GAE environment.
 * 
 * @see <a
 *      href="http://code.google.com/appengine/docs/java/howto/unittesting.html">Unit
 *      Testing in Appengine</a>
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class TestBase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(TestBase.class.getName());

	/** */
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
	// Our tests assume strong consistency
		new LocalDatastoreServiceTestConfig(),// .setDefaultHighRepJobPolicyUnappliedJobPercentage(100),
		new LocalMemcacheServiceTestConfig(), new LocalTaskQueueTestConfig());

	/** */
	@Before
	public void setUp()
	{
		this.helper.setUp();
	}

	/** */
	@After
	public void tearDown()
	{
		AsyncCacheFilter.complete();
		ObjectifyService.reset();
		this.helper.tearDown();
	}

	/**
	 * Utility methods that puts, clears the session, and immediately gets an
	 * entity
	 */
	protected <T> T putClearGet(T saveMe)
	{

		Key<T> key = ObjectifyService.ofy().save().entity(saveMe).now();

		try
		{
			Entity ent = ds().get(null, key.getRaw());
			System.out.println(ent);
		}
		catch (EntityNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		ObjectifyService.ofy().clear();

		return ObjectifyService.ofy().load().key(key).get();
	}

	/** Get a DatastoreService */
	protected DatastoreService ds()
	{
		return DatastoreServiceFactory.getDatastoreService();
	}

	/** Useful utility method */
	protected void assertRefUninitialzied(Ref<?> ref)
	{
		try
		{
			ref.get();
			assert false;
		}
		catch (IllegalStateException ex)
		{
		}
	}

}