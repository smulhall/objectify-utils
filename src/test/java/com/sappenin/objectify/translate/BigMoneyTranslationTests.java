package com.sappenin.objectify.translate;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.testng.annotations.Test;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.test.util.TestBase;
import com.sappenin.objectify.annotation.Money;

/**
 * Tests of type conversions.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public class BigMoneyTranslationTests extends TestBase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(BigMoneyTranslationTests.class.getName());

	/** */
	@com.googlecode.objectify.annotation.Entity
	@Cache
	public static class HasBigMoney
	{
		@Id
		public Long id;

		@Money
		public org.joda.money.Money moneyAmount;

		@Money(currencyCodeFieldName = "ccfn", encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexCurrencyCode = true, indexDisplayableAmount = true, indexEncodedAmount = true, storeDisplayableAmount = true)
		public org.joda.money.Money moneyAmountAll;

		@Money(currencyCodeFieldName = "ccfn", encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexCurrencyCode = false, indexDisplayableAmount = false, indexEncodedAmount = false, storeDisplayableAmount = true)
		public org.joda.money.Money moneyAmountAllNoIndices;

		@Money(storeDisplayableAmount = false)
		public org.joda.money.Money moneyAmountNoDisplayableAmount;

		@Money
		public org.joda.money.BigMoney bigMoneyAmount;

		@Money(currencyCodeFieldName = "ccfn", encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexCurrencyCode = true, indexDisplayableAmount = true, indexEncodedAmount = true, storeDisplayableAmount = true)
		public org.joda.money.BigMoney bigMoneyAmountAll;

		@Money(currencyCodeFieldName = "ccfn", encodedAmountFieldName = "eafn", displayableAmountFieldName = "dafn", indexCurrencyCode = false, indexDisplayableAmount = false, indexEncodedAmount = false, storeDisplayableAmount = true)
		public org.joda.money.BigMoney bigMoneyAmountAllNoIndices;

		@Money(storeDisplayableAmount = false)
		public org.joda.money.BigMoney bigMoneyAmountNoDisplayableAmount;

	}

	/** */
	@Test
	public void testBigMoneyConverter() throws Exception
	{
		// fact.getTranslators().add(new BigDecimalStringTranslatorFactory());
		fact.getTranslators().add(new JodaMoneyTranslatorFactory());
		fact.register(HasBigMoney.class);

		HasBigMoney hbm = new HasBigMoney();
		hbm.moneyAmount = org.joda.money.Money.of(CurrencyUnit.USD, new BigDecimal("32.25"));
		hbm.moneyAmountAll = org.joda.money.Money.of(CurrencyUnit.USD, new BigDecimal("1856465.25"));
		hbm.moneyAmountAllNoIndices = org.joda.money.Money.of(CurrencyUnit.USD, new BigDecimal("54648979841"));
		hbm.moneyAmountNoDisplayableAmount = org.joda.money.Money
			.of(CurrencyUnit.USD, new BigDecimal("54648979841.25"));

		hbm.bigMoneyAmount = BigMoney.of(CurrencyUnit.USD, new BigDecimal("32.255698"));
		hbm.bigMoneyAmountAll = BigMoney.of(CurrencyUnit.USD, new BigDecimal("1856465.255698"));
		hbm.bigMoneyAmountAllNoIndices = BigMoney.of(CurrencyUnit.USD, new BigDecimal("54648979841"));
		hbm.bigMoneyAmountNoDisplayableAmount = BigMoney.of(CurrencyUnit.USD, new BigDecimal("54648979841.25646845"));

		HasBigMoney fetched = this.putClearGet(hbm);

		assert hbm.moneyAmount.equals(fetched.moneyAmount);
		assert hbm.moneyAmountAll.equals(fetched.moneyAmountAll);
		assert hbm.moneyAmountAllNoIndices.equals(fetched.moneyAmountAllNoIndices);
		assert hbm.moneyAmountNoDisplayableAmount.equals(fetched.moneyAmountNoDisplayableAmount);

		assert hbm.bigMoneyAmount.equals(fetched.bigMoneyAmount);
		assert hbm.bigMoneyAmountAll.equals(fetched.bigMoneyAmountAll);
		assert hbm.bigMoneyAmountAllNoIndices.equals(fetched.bigMoneyAmountAllNoIndices);
		assert hbm.bigMoneyAmountNoDisplayableAmount.equals(fetched.bigMoneyAmountNoDisplayableAmount);

	}

}