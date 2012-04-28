package com.sappenin.objectify.translate.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.testng.annotations.Test;

/**
 * Tests the number encoding scheme
 * 
 * @author ted stockwell
 * @author David Fuelling
 */
public class BigDecimalCodecTest
{

	@Test
	public void testNumberDecoder() throws ParseException
	{
		String[] tokens = new String[] {
			"-100.5", "**6899:4?", "-10.5", "**789:4?", "-3.145", "*6:854?", "-3.14", "*6:85?", "-1.01", "*8:98?",
			"-1", "*8?", "-0.0001233", "*9:9998766?", "-0.000123", "*9:999876?", "0", "?0*", "0.000123", "?0.000123*",
			"0.0001233", "?0.0001233*", "1", "?1*", "1.01", "?1.01*", "3.14", "?3.14*", "3.145", "?3.145*", "10.5",
			"??210.5*", "100.5", "??3100.5*"
		};

		// test the ordering of the characters used in the encoding
		// if characters are not ordered in correct manner then encoding fails
		Assert.assertTrue('*' < '.');
		Assert.assertTrue('.' < '0');
		Assert.assertTrue('0' < '1');
		Assert.assertTrue('1' < '2');
		Assert.assertTrue('2' < '3');
		Assert.assertTrue('3' < '4');
		Assert.assertTrue('4' < '5');
		Assert.assertTrue('5' < '6');
		Assert.assertTrue('6' < '7');
		Assert.assertTrue('7' < '8');
		Assert.assertTrue('8' < '9');
		Assert.assertTrue('9' < ':');
		Assert.assertTrue(':' < '?');

		ArrayList<String> encoded = new ArrayList<String>();

		// test that the above encoded numbers are actually in lexical order
		// like we think they are
		for (int i = 0; i < tokens.length; i = i + 2)
		{
			encoded.add(tokens[i + 1]);
		}
		Collections.sort(encoded);
		System.out.print(encoded);
		for (int i = 0, j = 0; i < tokens.length; i = i + 2)
		{
			Assert.assertEquals(tokens[i + 1], encoded.get(j++));
		}

		// test that we decode correctly
		for (int i = 0; i < tokens.length; i = i + 2)
		{
			Assert.assertEquals(tokens[i], BigDecimalCodec.decode(tokens[i + 1]));
		}

		// test that we encode correctly
		for (int i = 0; i < tokens.length; i = i + 2)
		{
			Assert.assertEquals(tokens[i + 1], BigDecimalCodec.encode(tokens[i]));
		}
	}

	@Test
	public void testAdditionalEncodingsWithBigDecimals()
	{
		String[] encodedTokens = new String[] {
			"**6899:49?", "**789:49?", "*6:84?", "*6:85?", "*8:98?", "*8:99?", "?0.00*", "?0.00*", "?0.00*", "?0.00*",
			"?0.00*", "?1.00*", "?1.01*", "?3.14*", "?3.15*", "??210.50*", "??3100.50*"
		};

		BigDecimal[] tokens = new BigDecimal[] {
			createBigDecimal("-100.5"), // Translates to -100.50
			createBigDecimal("-10.5"), // Translates to -10.50
			createBigDecimal("-3.145"), // Translates to -3.14
			createBigDecimal("-3.14"),// Translates to -3.14
			createBigDecimal("-1.01"), // Translates to -1.01
			createBigDecimal("-1"),// Translates to -1.00
			createBigDecimal("-0.0001233"), // Translates to -0.00
			createBigDecimal("-0.000123"),// Translates to -0.00
			createBigDecimal("0"), // Translates to 0.00
			createBigDecimal("0.000123"),// Translates to 0.00
			createBigDecimal("0.0001233"), // Translates to 0.00
			createBigDecimal("1"),// Translates to 1.00
			createBigDecimal("1.01"), // Translates to 1.01
			createBigDecimal("3.14"),// Translates to 3.14
			createBigDecimal("3.145"), // Translates to 3.15
			createBigDecimal("10.5"),// Translates to 10.50
			createBigDecimal("100.5")
		// Translates to 100.50
		};

		// test that we encode correctly
		for (int i = 0; i < tokens.length; i++)
		{
			// /////////////////////////
			// Test this way...
			// /////////////////////////

			String preEncodedToken = encodedTokens[i];
			String encodedToken = BigDecimalCodec.encode(tokens[i]);
			Assert.assertEquals(preEncodedToken, encodedToken);

			// /////////////////////////
			// Then test this way...
			// /////////////////////////
			String baselineEncodedToken = encodedTokens[i];

			BigDecimal bd = tokens[i];
			String encodedBD = BigDecimalCodec.encode(bd);

			Assert.assertEquals(baselineEncodedToken, encodedBD);
		}
		// test that we decode correctly
		for (int i = 0; i < tokens.length; i++)
		{
			// /////////////////////////
			// Test this way...
			// /////////////////////////
			Assert.assertEquals(tokens[i], BigDecimalCodec.decodeAsBigDecimal(encodedTokens[i]));

			// /////////////////////////
			// Then test this way...
			// /////////////////////////
			BigDecimal bd = tokens[i];

			String encodedToken = encodedTokens[i];
			BigDecimal decodedBD = BigDecimalCodec.decodeAsBigDecimal(encodedToken);

			Assert.assertEquals(bd, decodedBD);
		}
	}

	/**
	 * Helper to create a java.math.BigDecimal.
	 * 
	 * @param string
	 * @return
	 */
	protected BigDecimal createBigDecimal(String value)
	{
		BigDecimal returnValue = new BigDecimal(value);
		returnValue = returnValue.setScale(2, BigDecimal.ROUND_HALF_UP);
		return returnValue;
	}
}
