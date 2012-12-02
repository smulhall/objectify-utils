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
package com.sappenin.objectify.translate;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.test.util.TestBase;

/**
 * Test cases for {@link UTCReadableInstantTranslatorFactory}.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public class UTCReadableInstantTranslatorFactoryTest extends TestBase
{
	/** */
	@com.googlecode.objectify.annotation.Entity
	@Cache
	public static class HasReadableInstant
	{
		@Id
		public Long id;

		public DateTime now;

		public DateTime nowPlus1Hours;

		public DateTime nowPlus3Hours;

		public DateTime nowPlus12Hours;

		public DateTime nowPlus23Hours;

		public DateTime nowPlus24Hours;

		public DateTime nowPlus25Hours;

		public DateTime nowPlus2Weeks;

		public DateTime nowPlus1Month;

		public DateTime nowPlus1Year;

	}

	@Before
	public void setUp()
	{
		super.setUp();
	}

	@Test
	public void testDateTimeUTCConverter() throws Exception
	{
		ObjectifyService.ofy().getFactory().getTranslators().add(new UTCReadableInstantTranslatorFactory());
		ObjectifyService.ofy().getFactory().register(HasReadableInstant.class);

		HasReadableInstant hri = new HasReadableInstant();
		hri.now = DateTime.now(DateTimeZone.UTC);
		hri.nowPlus1Hours = DateTime.now(DateTimeZone.UTC).plusHours(1);
		hri.nowPlus3Hours = DateTime.now(DateTimeZone.UTC).plusHours(3);
		hri.nowPlus12Hours = DateTime.now(DateTimeZone.UTC).plusHours(12);
		hri.nowPlus23Hours = DateTime.now(DateTimeZone.UTC).plusHours(23);
		hri.nowPlus24Hours = DateTime.now(DateTimeZone.UTC).plusHours(24);
		hri.nowPlus25Hours = DateTime.now(DateTimeZone.UTC).plusHours(25);
		hri.nowPlus2Weeks = DateTime.now(DateTimeZone.UTC).plusWeeks(2);
		hri.nowPlus1Month = DateTime.now(DateTimeZone.UTC).plusMonths(1);
		hri.nowPlus1Year = DateTime.now(DateTimeZone.UTC).plusYears(1);

		HasReadableInstant fetched = this.putClearGet(hri);

		// Verify .equals equality
		assertEquals(hri.now, fetched.now);

		assertEquals(hri.nowPlus1Hours, fetched.nowPlus1Hours);
		assertEquals(hri.nowPlus3Hours, fetched.nowPlus3Hours);
		assertEquals(hri.nowPlus12Hours, fetched.nowPlus12Hours);
		assertEquals(hri.nowPlus23Hours, fetched.nowPlus23Hours);
		assertEquals(hri.nowPlus24Hours, fetched.nowPlus24Hours);
		assertEquals(hri.nowPlus25Hours, fetched.nowPlus25Hours);
		assertEquals(hri.nowPlus2Weeks, fetched.nowPlus2Weeks);
		assertEquals(hri.nowPlus2Weeks, fetched.nowPlus2Weeks);
		assertEquals(hri.nowPlus2Weeks, fetched.nowPlus2Weeks);

		// Verify .compareTo equality
		assertEquals(0, hri.now.compareTo(fetched.now));
		assertEquals(0, hri.nowPlus1Hours.compareTo(fetched.nowPlus1Hours));
		assertEquals(0, hri.nowPlus3Hours.compareTo(fetched.nowPlus3Hours));
		assertEquals(0, hri.nowPlus12Hours.compareTo(fetched.nowPlus12Hours));
		assertEquals(0, hri.nowPlus23Hours.compareTo(fetched.nowPlus23Hours));
		assertEquals(0, hri.nowPlus24Hours.compareTo(fetched.nowPlus24Hours));
		assertEquals(0, hri.nowPlus25Hours.compareTo(fetched.nowPlus25Hours));
		assertEquals(0, hri.nowPlus2Weeks.compareTo(fetched.nowPlus2Weeks));
		assertEquals(0, hri.nowPlus1Month.compareTo(fetched.nowPlus1Month));
		assertEquals(0, hri.nowPlus1Year.compareTo(fetched.nowPlus1Year));
	}
}
