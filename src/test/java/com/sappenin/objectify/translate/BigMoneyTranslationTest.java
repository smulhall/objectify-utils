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

import java.math.BigDecimal;

import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;
import org.junit.Test;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.test.util.TestBase;
import com.sappenin.objectify.annotation.Money;

/**
 * Tests of type conversions.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public class BigMoneyTranslationTest extends TestBase
{
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
		ObjectifyService.ofy().getFactory().getTranslators().add(new JodaMoneyTranslatorFactory());
		ObjectifyService.ofy().getFactory().register(HasBigMoney.class);

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

		assertEquals(hbm.moneyAmount, fetched.moneyAmount);
		assertEquals(hbm.moneyAmountAll, fetched.moneyAmountAll);
		assertEquals(hbm.moneyAmountAllNoIndices, fetched.moneyAmountAllNoIndices);
		assertEquals(hbm.moneyAmountNoDisplayableAmount, fetched.moneyAmountNoDisplayableAmount);

		assertEquals(hbm.bigMoneyAmount, fetched.bigMoneyAmount);
		assertEquals(hbm.bigMoneyAmountAll, fetched.bigMoneyAmountAll);
		assertEquals(hbm.bigMoneyAmountAllNoIndices, fetched.bigMoneyAmountAllNoIndices);
		assertEquals(hbm.bigMoneyAmountNoDisplayableAmount, fetched.bigMoneyAmountNoDisplayableAmount);

	}

}