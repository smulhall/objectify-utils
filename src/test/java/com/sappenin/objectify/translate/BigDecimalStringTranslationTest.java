package com.sappenin.objectify.translate;

import java.math.BigDecimal;

import org.testng.annotations.Test;

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
		fact.getTranslators().add(new BigDecimalStringTranslatorFactory());
		fact.register(HasBigDecimal.class);

		HasBigDecimal hbd = new HasBigDecimal();
		hbd.bigDecimalAmount = new BigDecimal("32.25");
		hbd.bigDecimalAmountAll = new BigDecimal("1856465.25");
		hbd.bigDecimalAmountAllNoIndices = new BigDecimal("54648979841");
		hbd.bigDecimalAmountNoDisplayableAmount = new BigDecimal("54648979841.25");

		HasBigDecimal fetched = this.putClearGet(hbd);

		assert hbd.bigDecimalAmount.equals(fetched.bigDecimalAmount);
		assert hbd.bigDecimalAmountAll.equals(fetched.bigDecimalAmountAll);
		assert hbd.bigDecimalAmountAllNoIndices.equals(fetched.bigDecimalAmountAllNoIndices);
		assert hbd.bigDecimalAmountNoDisplayableAmount.equals(fetched.bigDecimalAmountNoDisplayableAmount);

	}

}