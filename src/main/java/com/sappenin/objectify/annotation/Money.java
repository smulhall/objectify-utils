package com.sappenin.objectify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.joda.money.BigMoney;

/**
 * <p>
 * When placed on an entity field of type {@link Money} or {@link BigMoney}, the
 * following properties will be stored for that joda money property:
 * <ul>
 * <li>The currency value, encoded as a String.</li>
 * <li>The currency code, as a String</li>
 * <li>(Optional) Currency value in human-readable format (enabled by default)</li>
 * </ul>
 * </p>
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.FIELD
})
public @interface Money
{
	boolean storeDisplayableAmount() default true;

	String displayableAmountFieldName() default "displayableAmount";

	boolean indexDisplayableAmount() default false;

	String encodedAmountFieldName() default "encodedAmount";

	boolean indexEncodedAmount() default false;

	String currencyCodeFieldName() default "currencyCode";

	boolean indexCurrencyCode() default false;

}