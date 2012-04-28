package com.sappenin.objectify.translate;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.Property;
import com.googlecode.objectify.impl.translate.CreateContext;
import com.googlecode.objectify.impl.translate.LoadContext;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.ValueTranslator;
import com.googlecode.objectify.impl.translate.ValueTranslatorFactory;
import com.sappenin.objectify.translate.util.BigDecimalCodec;

/**
 * <p>
 * This a more advanced strategy for storing BigDecimal in the datastore.
 * BigDecimalStringTranslatorFactory encodes the BigDecimal value and stores the
 * result as a String. This is appropriate for monetary and other values of ~475
 * digits (the end encoded value length is number dependent). This strategy
 * offers the following advantages over encoding a BigDecimal as a Long:
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
public class BigDecimalStringTranslatorFactory extends ValueTranslatorFactory<BigDecimal, String>
{
	/**
	 * Construct this converter with the default factor (1000), which can store
	 * three points of precision past the decimal point.
	 */
	public BigDecimalStringTranslatorFactory()
	{
		super(BigDecimal.class);
	}

	@Override
	protected ValueTranslator<BigDecimal, String> createSafe(Path path, Property property, Type type, CreateContext ctx)
	{
		return new ValueTranslator<BigDecimal, String>(path, String.class)
		{
			@Override
			protected BigDecimal loadValue(String value, LoadContext ctx)
			{
				return BigDecimalCodec.decodeAsBigDecimal(value.toString());
			}

			@Override
			protected String saveValue(BigDecimal value, SaveContext ctx)
			{
				return BigDecimalCodec.encode(value);
			}
		};
	}
}