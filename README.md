objectify-utils (Objectify Utilities for App Engine)
===========================

This project contains several utility and extension classes to enhance <a href="http://code.google.com/p/objectify-appengine">Objectify 4</a> with capabilities related large numbers and Money using Joda Money.

First, download the latest <a href="https://github.com/sappenin/objectify-utils/raw/master/maven/com/sappenin/objectify/objectify-utils/1.0.0/objectify-utils-1.0.0.jar">objectify-utils-1.0.0.jar</a> and include it your application's classpath.

Maven users should utilize the following repository and dependency instead:

	<repositories>
		<repository>
			<id>sappenin-objectify-utils</id>
			<url>https://github.com/sappenin/objectify-utils/tree/master/maven</url>
		</repository>
	</repositories>

    <dependency>
    	<groupId>com.sappenin.objectify</groupId>
		<artifactId>objectify-utils</artifactId>
		<version>1.0.0</version>
    </dependency>

Next, be sure to register each annotation that you plan to use, as follows:

	ObjectifyService.factory().getTranslators().add(new BigDecimalStringTranslatorFactory());
	ObjectifyService.factory().getTranslators().add(new JodaMoneyTranslatorFactory());


BigDecimal Entity Fields
-------
To persist properties of type java.math.BigDecimal, annotate your field with the @com.sappenin.objectify.annotations.BigDecimal.  Be sure to not confuse this with the default BigDecimal support provided by Objectify which doesn't handle indexing properly (see <a href="http://code.google.com/p/objectify-appengine/source/browse/trunk/src/com/googlecode/objectify/impl/translate/opt/BigDecimalLongTranslatorFactory.java">here</a>).    

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

    
Authors
-------

**David Fuelling**

+ sappenin@gmail.com
+ http://twitter.com/sappenin
+ http://github.com/sappenin


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