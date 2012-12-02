Objectify Translators
===========================

Features
------

+ <b>Enhanced Objectify Translators for Joda ReadableInstance Types</b><br/>
Ensure your entities have Date/Time properties that always use UTC for consistent load/store behavior.  <a href="https://github.com/sappenin/objectify-utils/blob/master/src/main/java/com/sappenin/objectify/translate/UTCReadableInstantTranslatorFactory.java">UTCReadableInstantTranslatorFactory</a> 
handles loading and saving of any property with type <a href="http://joda-time.sourceforge.net/apidocs/org/joda/time/ReadableInstant.html">org.joda.time.ReadableInstance</a>.

+ <b>Enhanced Objectify Annotations and Translators for Joda "Money" Types</b><br/>
Store entities with Joda-Money properties in interesting ways, fully controllable via field annotations.  See the <a href="https://github.com/sappenin/objectify-utils#benefits-of-new-money-translators">Benefits</a> section below.


Benefits of New "Money" Translators
------

+ <b>Fully Indexable BigDecimal and Money Fields</b><br/>
Whether your property is of type BigDecimal, Money, or BigMoney, the Translators in objectify-utils store all number values in an encoded String-format that is lexigraphically equivalent to numeric values when it comes to comparison.  This encoding format supports negative values, and means currency values can be fully indexed, sorted, and queried natively via the Datastore.   

+ <b>Arbitrary Precision</b><br/>
Objectify-utils translators allows for arbitrary number-precision across and inside entities.  For example, one "Car" entity with a "value" property of "$25,000.00" could be stored while another "Car" could have a more precise value of "$25,000.253".

+ <b>Anotation Support for Joda Money and BigMoney</b><br/>
Joda-Money and Joda-BigMoney both implement a common interface (BigMoneyProvider), making it possible to utilize the same translator for both object types.  

+ <b>Full Currency Code Support</b><br/>
JodaMoneyTranslatorFactory can store a currency-code for any Money/BigMoney object in a different embedded field that is related to the currency value amount.

+ <b>Full Index Control and Field Name Customization</b><br/>
Using the @BigDecimal and @Money annotations, you can control how your Number and Currency information is stored, what is indexed, and what each embedded field is called.


Getting Started
----------
Be sure to register each annotation that you plan to use, as follows:

	ObjectifyService.factory().getTranslators().add(new BigDecimalStringTranslatorFactory());
	ObjectifyService.factory().getTranslators().add(new JodaMoneyTranslatorFactory());
	ObjectifyService.factory().getTranslators().add(new UTCReadableInstantTranslatorFactory());

BigDecimal Entity Fields
-------
To persist properties of type java.math.BigDecimal, annotate your field with the @com.sappenin.objectify.annotations.BigDecimal.  Be sure to not confuse this with the default BigDecimal support provided by Objectify which doesn't handle indexing properly (see <a href="http://code.google.com/p/objectify-appengine/source/browse/src/main/java/com/googlecode/objectify/impl/translate/opt/BigDecimalLongTranslatorFactory.java">here</a>).    

Example configuration:

    @Entity
    public class OfyEntity
	{
   		
   		... //Rest of Objectify4 Entity definition
   	
   		@BigDecimal
    	BigDecimal bigDecimal;
	}

Joda-Money  Entity Fields
-------
To persist properties of type com.joda.money.Money or com.joda.money.BigMoney, annotate your field with the @com.sappenin.objectify.annotations.Money. 

Example configuration:

	@Entity
    public class OfyEntity
	{
   		
   		... //Rest of Objectify4 Entity definition
   	
    	@Money
    	BigMoney moneyAmount;

	}

Copyright and License
---------------------

Copyright 2012 Sappenin Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.