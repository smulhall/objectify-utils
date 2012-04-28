package com.sappenin.objectify.translate;

import java.lang.reflect.Type;
import java.math.BigDecimal;

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
import com.sappenin.objectify.translate.util.BigDecimalCodec;

/**
 * <p>
 * This a more advanced strategy for storing java.math.BigDecimal in the
 * datastore. BigDecimalStringTranslatorFactory encodes the java.math.BigDecimal
 * value and stores the result as a String. This is appropriate for monetary and
 * other values of ~475 digits (the end encoded value length is number
 * dependent). This strategy offers the following advantages over encoding a
 * java.math.BigDecimal as a Long:
 * <ul>
 * <li>Offers support for arbitrary precision numbers</li>
 * <li>Supports very large numbers (~475 digits long)</li>
 * <li>Encodes numbers in lexigraphical order, which allows for native sorting
 * in Datastore queries.
 * </ul>
 * </p>
 * 
 * This translator is not installed by default, but can be installed as follows:
 * 
 * <pre>
 * ObjectifyService.factory().getTranslators().add(new BigDecimalStringTranslatorFactory());
 * </pre>
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
public class BigDecimalStringTranslatorFactory implements TranslatorFactory<BigDecimal>
{
	@Override
	public BigDecimalTranslator create(final Path path, final Property property, final Type type, CreateContext ctx)
	{
		if (java.math.BigDecimal.class.isAssignableFrom(GenericTypeReflector.erase(type)))
		{
			return new BigDecimalTranslator(property, type, ctx);
		}
		else
		{
			return null;
		}

	}

	/**
	 * Translator which knows what to do with a {@link java.math.BigDecimal}.<br/>
	 * <br/>
	 * This class utilizes an optional
	 * {@link com.sappenin.objectify.annotation.BigDecimal} annotation which
	 * allows for fine-grained control over the field names used to store the
	 * java.math.BigDecimal information, as well as indexing of each sub-field.
	 * See the Javadoc of that annotation for more details.
	 * 
	 * @author David Fuelling <sappenin@gmail.com>
	 */
	static class BigDecimalTranslator extends AbstractTranslator<BigDecimal>
	{
		private boolean storeDisplayableAmount;
		private boolean indexDisplayableAmount;
		private boolean indexAmount;

		private String encodedAmountFieldName = "encodedAmount";
		private String displayableAmountFieldName = "displayableAmount";

		/** */
		public BigDecimalTranslator(final Property property, final Type type, final CreateContext ctx)
		{
			super();

			@SuppressWarnings("unchecked")
			final Class<BigDecimal> clazz = (Class<BigDecimal>) GenericTypeReflector.erase(type);

			// Look for an @BigDecimal Annotation, if present.
			com.sappenin.objectify.annotation.BigDecimal bigDecimalAnnotation = TypeUtils.getAnnotation(
				com.sappenin.objectify.annotation.BigDecimal.class, property, clazz);
			if (bigDecimalAnnotation != null)
			{
				storeDisplayableAmount = bigDecimalAnnotation.storeDisplayableAmount();
				indexAmount = bigDecimalAnnotation.indexEncodedAmount();

				indexDisplayableAmount = bigDecimalAnnotation.indexDisplayableAmount();
				encodedAmountFieldName = bigDecimalAnnotation.encodedAmountFieldName();
				displayableAmountFieldName = bigDecimalAnnotation.displayableAmountFieldName();
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
		protected BigDecimal loadAbstract(Node node, LoadContext ctx)
		{
			if (!node.hasMap())
			{
				node.getPath().throwIllegalState("Expected map of values but found: " + node);
			}

			BigDecimal returnable = null;

			// Get the amount as a java.math.BigDecimal and the currencyCode as
			// a String.
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
					System.err.print("Unable to Decode java.math.BigDecimal from encoded string \"" + amountValue
						+ "\"");
					e.printStackTrace();
				}

				returnable = bdValue;
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
		protected Node saveAbstract(BigDecimal pojo, Path path, boolean index, SaveContext ctx)
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

				// EncodedAmountPath
				Path encodedAmountPath = path.extend(encodedAmountFieldName);
				Node encodedAmountNode = new Node(encodedAmountPath);
				// Encode the Amount value as a String
				String encodedValue = BigDecimalCodec.encode(pojo);
				encodedAmountNode.setPropertyValue(encodedValue, indexAmount);
				returnableNode.addToMap(encodedAmountNode);

				// DisplayableAmountPath (This is never loaded)
				if (storeDisplayableAmount)
				{
					Path displayableAmountPath = path.extend(displayableAmountFieldName);
					Node displayableAmountNode = new Node(displayableAmountPath);
					displayableAmountNode.setPropertyValue(pojo.toString(), indexDisplayableAmount);
					returnableNode.addToMap(displayableAmountNode);
				}
				return returnableNode;
			}

		}
	}

}