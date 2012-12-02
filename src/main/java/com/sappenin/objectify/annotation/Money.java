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
package com.sappenin.objectify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.joda.money.BigMoney;

/**
 * <p>
 * When placed on an entity field of type {@link Money} or {@link BigMoney}, the
 * following properties will be stored for that property:
 * <ul>
 * <li>The currency value, encoded as a lexigraphically-encoded String.</li>
 * <li>The currency code, as a String</li>
 * <li>(Optional) Currency value in human-readable format (enabled by default)</li>
 * </ul>
 * </p>
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
 * <br/>
 * <li><b>{@code indexCurrencyCode}</b>: Set to {@code true} to index the
 * currencyCode property (<b>defaults to {@code false}</b>).</li>
 * <br/>
 * <li><b>{@code currencyCodeFieldName}</b>: The property-name to store the
 * {@code currencyCode} value in Appengine Datastore Entities. (<b>defaults to
 * {@code 'currencyCode'}</b>).</li>
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

	boolean indexDisplayableAmount() default false;

	String displayableAmountFieldName() default "displayableAmount";

	String encodedAmountFieldName() default "encodedAmount";

	boolean indexEncodedAmount() default true;

	String currencyCodeFieldName() default "currencyCode";

	boolean indexCurrencyCode() default false;

}