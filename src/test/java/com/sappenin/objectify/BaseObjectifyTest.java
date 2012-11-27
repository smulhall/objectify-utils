package com.sappenin.objectify;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;

/**
 * All tests should extend this class to set up the GAE environment.
 * 
 * @see <a
 *      href="http://code.google.com/appengine/docs/java/howto/unittesting.html">Unit
 *      Testing in Appengine</a>
 */
public class BaseObjectifyTest
{
	protected LocalServiceTestHelper helper = new LocalServiceTestHelper(
		// Our tests assume strong consistency, but a bug in the appengine test
		// harness forces us to set this value to at least 1.
		new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(1),
		new LocalMemcacheServiceTestConfig(), new LocalTaskQueueTestConfig());

	protected MemcacheService memcache;

	@Before
	public void setUp() throws Exception
	{
		this.helper.setUp();

		memcache = MemcacheServiceFactory.getMemcacheService();
	}

	@After
	public void tearDown()
	{
		AsyncCacheFilter.complete();
		ObjectifyService.reset();
		this.helper.tearDown();
	}

}