package com.sappenin.objectify.translate;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.money.BigMoney;
import org.joda.money.BigMoneyProvider;
import org.joda.money.CurrencyUnit;

import com.googlecode.objectify.impl.Node;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.TypeUtils;
import com.googlecode.objectify.impl.translate.AbstractTranslator;
import com.googlecode.objectify.impl.translate.CreateContext;
import com.googlecode.objectify.impl.translate.LoadContext;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.TranslatorFactory;
import com.googlecode.objectify.repackaged.gentyref.GenericTypeReflector;
import com.sappenin.objectify.annotation.Money;
import com.sappenin.objectify.translate.util.BigDecimalCodec;

/**
 * Factory for creating a {@link JodaMoneyTranslator} which can store component
 * properties of a {@link org.joda.money.BigMoneyProvider} as a "value" and a
 * "currencyUnit" (both of which are Strings), similar to how an Embedded class
 * would be stored. The {@code value} is an encoded-String version of a
 * {@link BigDecimal} that supports lexigraphical sorting and large-digit number
 * sets.
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public class JodaMoneyTranslatorFactory implements TranslatorFactory<BigMoneyProvider>
{

	@Override
	public JodaMoneyTranslator create(final Path path, final Property property, final Type type, CreateContext ctx)
	{
		@SuppressWarnings("unchecked")
		final Class<BigMoneyProvider> clazz = (Class<BigMoneyProvider>) GenericTypeReflector.erase(type);

		if (org.joda.money.BigMoney.class.isAssignableFrom(clazz))
		{
			return new JodaMoneyTranslator(property, type, ctx, true);
		}
		else if (org.joda.money.Money.class.isAssignableFrom(clazz))
		{
			return new JodaMoneyTranslator(property, type, ctx, false);
		}
		else
		{
			return null;
		}

	}
	/**
	 * Translator which knows what to do with a Joda
	 * {@link org.joda.money.BigMoneyProvider}, which is an interface
	 * implemented by both {@link org.joda.money.BigMoney} and
	 * {@link org.joda.money.Money} objects.<br/>
	 * <br/>
	 * This class utilizes an optional {@link Money} annotation which allows for
	 * fine-grained control over the field names used to store Money
	 * information, as well as indexing of each sub-field. See the Javadoc of
	 * that annotation for more details.
	 * 
	 * @author David Fuelling <sappenin@gmail.com>
	 */
	static class JodaMoneyTranslator extends AbstractTranslator<BigMoneyProvider>
	{
		private static final Logger log = Logger.getLogger(JodaMoneyTranslator.class.getName());

		// If false, use Money; Otherwise, use bigMoney
		private boolean isBigMoney = false;

		private String encodedAmountFieldName = "encodedAmount";
		private String displayableAmountFieldName = "displayableAmount";
		private String currencyCodeFieldName = "currencyCode";

		private boolean storeDisplayableAmount;
		private boolean indexDisplayableAmount;
		private boolean indexAmount;
		private boolean indexCurrencyCode;

		/** */
		public JodaMoneyTranslator(final Property property, final Type type, final CreateContext ctx, boolean isBigMoney)
		{
			super();

			this.isBigMoney = isBigMoney;

			@SuppressWarnings("unchecked")
			final Class<BigMoneyProvider> clazz = (Class<BigMoneyProvider>) GenericTypeReflector.erase(type);

			// Look for an @Money Annotation, if present.
			Money moneyAnnotation = TypeUtils.getAnnotation(Money.class, property, clazz);
			if (moneyAnnotation != null)
			{
				storeDisplayableAmount = moneyAnnotation.storeDisplayableAmount();
				indexCurrencyCode = moneyAnnotation.indexCurrencyCode();
				indexAmount = moneyAnnotation.indexEncodedAmount();

				indexDisplayableAmount = moneyAnnotation.indexDisplayableAmount();
				encodedAmountFieldName = moneyAnnotation.encodedAmountFieldName();
				displayableAmountFieldName = moneyAnnotation.displayableAmountFieldName();
				currencyCodeFieldName = moneyAnnotation.currencyCodeFieldName();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.googlecode.objectify.impl.translate.AbstractTranslator#loadAbstract
		 * (com.googlecode.objectify.impl.Node,
		 * com.googlecode.objectify.impl.translate.LoadContext)
		 */
		@Override
		protected BigMoneyProvider loadAbstract(Node node, LoadContext ctx)
		{
			if (!node.hasMap())
			{
				node.getPath().throwIllegalState("Expected map of values but found: " + node);
			}

			BigMoneyProvider returnable = null;

			// Get the amount as a BigDecimal and the currencyCode as a String.
			Node amountNode = node.get(encodedAmountFieldName);
			Object amountValue = amountNode.getPropertyValue();
			if ((amountValue != null) && (amountValue.toString().length() > 0))
			{

				// //////////
				// Get the CurrencyUnit, defaulting to USD
				// //////////
				BigDecimal bdValue = null;
				try
				{
					bdValue = BigDecimalCodec.decodeAsBigDecimal(amountValue.toString());
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "Unable to Decode BigDecimal from encoded string \"" + amountValue + "\"", e);
				}

				// //////////
				// Get the CurrencyUnit, defaulting to USD
				// //////////
				CurrencyUnit currencyUnit = CurrencyUnit.USD;
				Node currencyUnitNode = node.get(currencyCodeFieldName);
				if (currencyUnitNode.hasPropertyValue())
				{
					Object currencyCode = currencyUnitNode.getPropertyValue();
					if (currencyCode != null)
					{
						currencyUnit = CurrencyUnit.getInstance(currencyCode.toString());
					}
				}

				if (isBigMoney)
				{
					returnable = BigMoney.of(currencyUnit, bdValue);
				}
				else
				{
					returnable = org.joda.money.Money.of(currencyUnit, bdValue);
				}
			}

			return returnable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.googlecode.objectify.impl.translate.AbstractTranslator#saveAbstract
		 * (java.lang.Object, com.googlecode.objectify.impl.Path, boolean,
		 * com.googlecode.objectify.impl.translate.SaveContext)
		 */
		@Override
		protected Node saveAbstract(BigMoneyProvider pojo, Path path, boolean index, SaveContext ctx)
		{
			if (pojo == null)
			{
				Node node = new Node(path);
				node.setPropertyValue(null, index);
				return node;
			}
			else
			{
				Node returnableNode = new Node(path);

				BigMoney bigMoney = pojo.toBigMoney();

				// EncodedAmountPath
				Path encodedAmountPath = path.extend(encodedAmountFieldName);
				Node encodedAmountNode = new Node(encodedAmountPath);
				// Encode the Amount value as a String
				String encodedValue = BigDecimalCodec.encode(bigMoney.getAmount());
				encodedAmountNode.setPropertyValue(encodedValue, indexAmount);
				returnableNode.addToMap(encodedAmountNode);

				// DisplayableAmountPath (This is never loaded)
				if (storeDisplayableAmount)
				{
					Path displayableAmountPath = path.extend(displayableAmountFieldName);
					Node displayableAmountNode = new Node(displayableAmountPath);
					displayableAmountNode.setPropertyValue(bigMoney.toString(), indexDisplayableAmount);
					returnableNode.addToMap(displayableAmountNode);
				}

				// CurrencyCodePath
				Path ccPath = path.extend(currencyCodeFieldName);
				Node ccNode = new Node(ccPath);
				ccNode.setPropertyValue(bigMoney.getCurrencyUnit().getCurrencyCode(), indexCurrencyCode);
				returnableNode.addToMap(ccNode);

				return returnableNode;
				// return this.savePropertyValue(pojo, path, index, ctx);
			}

		}
	}

}