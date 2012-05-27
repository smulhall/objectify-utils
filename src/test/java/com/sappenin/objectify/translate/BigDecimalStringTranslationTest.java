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
		fact.getTranslators().add(new BigDecimalStringTranslatorFactory());
		fact.register(HasBigDecimal.class);

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

		assert hbd.bigDecimalAmount.equals(fetched.bigDecimalAmount);
		assert hbd.bigDecimalAmountAll.equals(fetched.bigDecimalAmountAll);
		assert hbd.bigDecimalAmountWith3Scale.equals(fetched.bigDecimalAmountWith3Scale);
		assert hbd.bigDecimalAmountWith2Zeros.equals(fetched.bigDecimalAmountWith2Zeros);
		assert hbd.bigDecimalAmountWith3Zeros.equals(fetched.bigDecimalAmountWith3Zeros);
		assert hbd.bigDecimalAmountWith4Zeros.equals(fetched.bigDecimalAmountWith4Zeros);
		assert hbd.bigDecimalAmountWith5Zeros.equals(fetched.bigDecimalAmountWith5Zeros);
		assert hbd.bigDecimalAmountAllNoIndices.equals(fetched.bigDecimalAmountAllNoIndices);
		assert hbd.bigDecimalAmountNoDisplayableAmount.equals(fetched.bigDecimalAmountNoDisplayableAmount);

		assert hbd.bigDecimalAmount.compareTo(fetched.bigDecimalAmount) == 0;
		assert hbd.bigDecimalAmountAll.compareTo(fetched.bigDecimalAmountAll) == 0;
		assert hbd.bigDecimalAmountWith3Scale.compareTo(fetched.bigDecimalAmountWith3Scale) == 0;
		assert hbd.bigDecimalAmountWith2Zeros.compareTo(fetched.bigDecimalAmountWith2Zeros) == 0;
		assert hbd.bigDecimalAmountWith3Zeros.compareTo(fetched.bigDecimalAmountWith3Zeros) == 0;
		assert hbd.bigDecimalAmountWith4Zeros.compareTo(fetched.bigDecimalAmountWith4Zeros) == 0;
		assert hbd.bigDecimalAmountWith5Zeros.compareTo(fetched.bigDecimalAmountWith5Zeros) == 0;
		assert hbd.bigDecimalAmountAllNoIndices.compareTo(fetched.bigDecimalAmountAllNoIndices) == 0;
		assert hbd.bigDecimalAmountNoDisplayableAmount.compareTo(fetched.bigDecimalAmountNoDisplayableAmount) == 0;

	}

	@Test
	public void testBigDecimalStringConverterUsingIntConstructor() throws Exception
	{
		fact.getTranslators().add(new BigDecimalStringTranslatorFactory());
		fact.register(HasBigDecimal.class);

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

		assert hbd.bigDecimalAmount.equals(fetched.bigDecimalAmount);
		assert hbd.bigDecimalAmountAll.equals(fetched.bigDecimalAmountAll);
		assert hbd.bigDecimalAmountWith3Scale.equals(fetched.bigDecimalAmountWith3Scale);
		assert hbd.bigDecimalAmountWith2Zeros.equals(fetched.bigDecimalAmountWith2Zeros);
		assert hbd.bigDecimalAmountWith3Zeros.equals(fetched.bigDecimalAmountWith3Zeros);
		assert hbd.bigDecimalAmountWith4Zeros.equals(fetched.bigDecimalAmountWith4Zeros);
		assert hbd.bigDecimalAmountWith5Zeros.equals(fetched.bigDecimalAmountWith5Zeros);
		assert hbd.bigDecimalAmountAllNoIndices.equals(fetched.bigDecimalAmountAllNoIndices);
		assert hbd.bigDecimalAmountNoDisplayableAmount.equals(fetched.bigDecimalAmountNoDisplayableAmount);

		assert hbd.bigDecimalAmount.compareTo(fetched.bigDecimalAmount) == 0;
		assert hbd.bigDecimalAmountAll.compareTo(fetched.bigDecimalAmountAll) == 0;
		assert hbd.bigDecimalAmountWith3Scale.compareTo(fetched.bigDecimalAmountWith3Scale) == 0;
		assert hbd.bigDecimalAmountWith2Zeros.compareTo(fetched.bigDecimalAmountWith2Zeros) == 0;
		assert hbd.bigDecimalAmountWith3Zeros.compareTo(fetched.bigDecimalAmountWith3Zeros) == 0;
		assert hbd.bigDecimalAmountWith4Zeros.compareTo(fetched.bigDecimalAmountWith4Zeros) == 0;
		assert hbd.bigDecimalAmountWith5Zeros.compareTo(fetched.bigDecimalAmountWith5Zeros) == 0;
		assert hbd.bigDecimalAmountAllNoIndices.compareTo(fetched.bigDecimalAmountAllNoIndices) == 0;
		assert hbd.bigDecimalAmountNoDisplayableAmount.compareTo(fetched.bigDecimalAmountNoDisplayableAmount) == 0;

	}

}