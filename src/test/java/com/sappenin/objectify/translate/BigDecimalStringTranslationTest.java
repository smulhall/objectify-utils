package com.sappenin.objectify.translate;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.test.util.TestBase;

/**
 * Tests of type conversions.
 * 
 * @author David Fuelling
 */
public class BigDecimalStringTranslationTest extends TestBase
{
	/** */
	@com.googlecode.objectify.annotation.Entity
	@Cache
	public static class HasBigDecimal
	{
		@Id
		public Long id;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmount;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmountWith3Scale;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmountWith2Zeros;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmountWith3Zeros;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmountWith4Zeros;

		@com.sappenin.objectify.annotation.BigDecimal
		public BigDecimal bigDecimalAmountWith5Zeros;

		@com.sappenin.objectify.annotation.BigDecimal(encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexDisplayableAmount = true, indexEncodedAmount = true, storeDisplayableAmount = true)
		public BigDecimal bigDecimalAmountAll;

		@com.sappenin.objectify.annotation.BigDecimal(encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexDisplayableAmount = false, indexEncodedAmount = false, storeDisplayableAmount = true)
		public BigDecimal bigDecimalAmountAllNoIndices;

		@com.sappenin.objectify.annotation.BigDecimal(storeDisplayableAmount = false)
		public BigDecimal bigDecimalAmountNoDisplayableAmount;

	}

	/** */
	@Test
	public void testBigDecimalStringConverter() throws Exception
	{
		ObjectifyService.ofy().getFactory().getTranslators().add(new BigDecimalStringTranslatorFactory());
		ObjectifyService.ofy().getFactory().register(HasBigDecimal.class);

		HasBigDecimal hbd = new HasBigDecimal();
		hbd.bigDecimalAmount = new BigDecimal("32.25");
		hbd.bigDecimalAmountWith3Scale = new BigDecimal("23.953");
		hbd.bigDecimalAmountWith2Zeros = new BigDecimal("23.00");
		hbd.bigDecimalAmountWith3Zeros = new BigDecimal("75.000");
		hbd.bigDecimalAmountWith4Zeros = new BigDecimal("86.0000");
		hbd.bigDecimalAmountWith5Zeros = new BigDecimal("5978979.00000");
		hbd.bigDecimalAmountAll = new BigDecimal("1856465.25");
		hbd.bigDecimalAmountAllNoIndices = new BigDecimal("54648979841");
		hbd.bigDecimalAmountNoDisplayableAmount = new BigDecimal("54648979841.25");

		HasBigDecimal fetched = this.putClearGet(hbd);

		assertEquals(hbd.bigDecimalAmount, fetched.bigDecimalAmount);
		assertEquals(hbd.bigDecimalAmountAll, fetched.bigDecimalAmountAll);
		assertEquals(hbd.bigDecimalAmountWith3Scale, fetched.bigDecimalAmountWith3Scale);
		assertEquals(hbd.bigDecimalAmountWith2Zeros, fetched.bigDecimalAmountWith2Zeros);
		assertEquals(hbd.bigDecimalAmountWith3Zeros, fetched.bigDecimalAmountWith3Zeros);
		assertEquals(hbd.bigDecimalAmountWith4Zeros, fetched.bigDecimalAmountWith4Zeros);
		assertEquals(hbd.bigDecimalAmountWith5Zeros, fetched.bigDecimalAmountWith5Zeros);
		assertEquals(hbd.bigDecimalAmountAllNoIndices, fetched.bigDecimalAmountAllNoIndices);
		assertEquals(hbd.bigDecimalAmountNoDisplayableAmount, fetched.bigDecimalAmountNoDisplayableAmount);

		assertEquals(0, hbd.bigDecimalAmount.compareTo(fetched.bigDecimalAmount));
		assertEquals(0, hbd.bigDecimalAmountAll.compareTo(fetched.bigDecimalAmountAll));
		assertEquals(0, hbd.bigDecimalAmountWith3Scale.compareTo(fetched.bigDecimalAmountWith3Scale));
		assertEquals(0, hbd.bigDecimalAmountWith2Zeros.compareTo(fetched.bigDecimalAmountWith2Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith3Zeros.compareTo(fetched.bigDecimalAmountWith3Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith4Zeros.compareTo(fetched.bigDecimalAmountWith4Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith5Zeros.compareTo(fetched.bigDecimalAmountWith5Zeros));
		assertEquals(0, hbd.bigDecimalAmountAllNoIndices.compareTo(fetched.bigDecimalAmountAllNoIndices));
		assertEquals(0, hbd.bigDecimalAmountNoDisplayableAmount.compareTo(fetched.bigDecimalAmountNoDisplayableAmount));

	}

	@Test
	public void testBigDecimalStringConverterUsingIntConstructor() throws Exception
	{
		ObjectifyService.ofy().getFactory().getTranslators().add(new BigDecimalStringTranslatorFactory());
		ObjectifyService.ofy().getFactory().register(HasBigDecimal.class);

		HasBigDecimal hbd = new HasBigDecimal();
		hbd.bigDecimalAmount = new BigDecimal(3225);
		hbd.bigDecimalAmountWith3Scale = new BigDecimal(23.953f);
		hbd.bigDecimalAmountWith2Zeros = new BigDecimal(23.00);
		hbd.bigDecimalAmountWith3Zeros = new BigDecimal(75.000);
		hbd.bigDecimalAmountWith4Zeros = new BigDecimal(86.0000);
		hbd.bigDecimalAmountWith5Zeros = new BigDecimal(5978979.00000);
		hbd.bigDecimalAmountAll = new BigDecimal(1856465.25);
		hbd.bigDecimalAmountAllNoIndices = new BigDecimal(546489798);
		hbd.bigDecimalAmountNoDisplayableAmount = new BigDecimal(54648979841.25);

		HasBigDecimal fetched = this.putClearGet(hbd);

		assertEquals(hbd.bigDecimalAmount, fetched.bigDecimalAmount);
		assertEquals(hbd.bigDecimalAmountAll, fetched.bigDecimalAmountAll);
		assertEquals(hbd.bigDecimalAmountWith3Scale, fetched.bigDecimalAmountWith3Scale);
		assertEquals(hbd.bigDecimalAmountWith2Zeros, fetched.bigDecimalAmountWith2Zeros);
		assertEquals(hbd.bigDecimalAmountWith3Zeros, fetched.bigDecimalAmountWith3Zeros);
		assertEquals(hbd.bigDecimalAmountWith4Zeros, fetched.bigDecimalAmountWith4Zeros);
		assertEquals(hbd.bigDecimalAmountWith5Zeros, fetched.bigDecimalAmountWith5Zeros);
		assertEquals(hbd.bigDecimalAmountAllNoIndices, fetched.bigDecimalAmountAllNoIndices);
		assertEquals(hbd.bigDecimalAmountNoDisplayableAmount, fetched.bigDecimalAmountNoDisplayableAmount);

		assertEquals(0, hbd.bigDecimalAmount.compareTo(fetched.bigDecimalAmount));
		assertEquals(0, hbd.bigDecimalAmountAll.compareTo(fetched.bigDecimalAmountAll));
		assertEquals(0, hbd.bigDecimalAmountWith3Scale.compareTo(fetched.bigDecimalAmountWith3Scale));
		assertEquals(0, hbd.bigDecimalAmountWith2Zeros.compareTo(fetched.bigDecimalAmountWith2Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith3Zeros.compareTo(fetched.bigDecimalAmountWith3Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith4Zeros.compareTo(fetched.bigDecimalAmountWith4Zeros));
		assertEquals(0, hbd.bigDecimalAmountWith5Zeros.compareTo(fetched.bigDecimalAmountWith5Zeros));
		assertEquals(0, hbd.bigDecimalAmountAllNoIndices.compareTo(fetched.bigDecimalAmountAllNoIndices));
		assertEquals(0, hbd.bigDecimalAmountNoDisplayableAmount.compareTo(fetched.bigDecimalAmountNoDisplayableAmount));

	}
}