package com.sappenin.objectify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * When placed on an entity field of type {@link java.math.BigDecimal} , the
 * following properties will be stored for the property:
 * <ul>
 * <li>The BigDecimal value, encoded as a lexigraphically-encoded String.</li>
 * <li>(Optional) BigDecimal value in human-readable format (enabled by default)
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * To customize the behavior of this annotation, the following properties may be
 * set:
 * <ul>
 * <li><b>{@code storeDisplayableAmount}</b>: Set to {@code true} to store a
 * displayable currency amount for human-readability in the App Engine Datastore
 * Viewer (defaults to {@code true}).</li>
 * <br/>
 * <li><b>{@code indexDisplayableAmount}</b>: Set to to index the
 * displayableAmount property (<b>defaults to {@code false}</b>).<br/>
 * &nbsp;&nbsp;<i>Note that this value will not index properly, so this should
 * generally not be used</i>.</li>
 * <br/>
 * <li><b>{@code displayableAmountFieldName}</b>: The property-name to store the
 * {@code displayableAmount} in Appengine Datastore Entities. (<b>defaults to
 * {@code 'displayableAmount'}</b>).</li>
 * <br/>
 * <li><b>{@code encodedAmountFieldName}</b>: The property-name to store the
 * {@code encodedAmount} value in Appengine Datastore Entities. (<b>defaults to
 * {@code 'encodedAmount'}</b>).</li>
 * <br/>
 * <li><b>{@code indexEncodedAmount}</b>: Set to {@code true} to index the
 * encodedAmount property (<b>defaults to {@code true}</b>).</li>
 * </ul>
 * </p>
 * 
 * 
 * @author David Fuelling <sappenin@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
	ElementType.FIELD
})
public @interface BigDecimalOfy
{
	boolean storeDisplayableAmount() default true;

	boolean indexDisplayableAmount() default false;

	String displayableAmountFieldName() default "displayableAmount";

	String encodedAmountFieldName() default "encodedAmount";

	boolean indexEncodedAmount() default true;
}