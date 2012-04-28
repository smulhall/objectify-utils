package com.sappenin.objectify.translate;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.test.util.TestBase;

/**
 * Tests of type conversions.
 * 
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class BigDecimalStringTranslationTests extends TestBase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(BigDecimalStringTranslationTests.class.getName());

	/** */
	@com.googlecode.objectify.annotation.Entity
	@Cache
	public static class HasBigDecimal
	{
		public @Id
		Long id;
		public BigDecimal data;
	}

	/** */
	@Test
	public void testBigDecimalStringConverter() throws Exception
	{
		fact.getTranslators().add(new BigDecimalStringTranslatorFactory());
		fact.register(HasBigDecimal.class);

		HasBigDecimal hbd = new HasBigDecimal();

		hbd.data = new BigDecimal("32.255698");
		HasBigDecimal fetched = this.putClearGet(hbd);
		assert hbd.data.equals(fetched.data);

		hbd.data = new BigDecimal("32");
		fetched = this.putClearGet(hbd);
		assert hbd.data.equals(fetched.data);

		hbd.data = new BigDecimal("150.56546684564687864564");
		fetched = this.putClearGet(hbd);
		assert hbd.data.equals(fetched.data);

		hbd.data = new BigDecimal("56546684564687864564.25");
		fetched = this.putClearGet(hbd);
		assert hbd.data.equals(fetched.data);

	}

}