Objectify-Utils (Objectify Utilities for Google Appengine)
===========================
[![Build Status](https://travis-ci.org/sappenin/objectify-utils.png)](https://travis-ci.org/sappenin/objectify-utils)

Objectify-Utils contain various utility and extension classes to augment <a href="http://code.google.com/p/objectify-appengine">Objectify 4</a>.  This library includes Objectify Translators for enhanced handling of large numbers, Joda-Money, and Joda-Time types.  In addition, it includes a ShardedCounter implementation for high-throughput+consistent counters backed by the GAE Datastore.

Table of Contents
-------
+ <a href="https://github.com/sappenin/objectify-utils/blob/master/README-Translators.md">Enhanced Translators</a>
+ <a href="https://github.com/sappenin/objectify-utils/blob/master/README-ShardedCounter.md">ShardedCounter Service</a>

Features
------

+ <b>Enhanced Objectify Translators for Joda ReadableInstance Types</b><br/>
Ensure your entities have Date/Time properties that always use UTC for consistent load/store behavior.  <a href="https://github.com/sappenin/objectify-utils/blob/master/src/main/java/com/sappenin/objectify/translate/UTCReadableInstantTranslatorFactory.java">UTCReadableInstantTranslatorFactory</a> 
handles loading and saving of any property with type <a href="http://joda-time.sourceforge.net/apidocs/org/joda/time/ReadableInstant.html">org.joda.time.ReadableInstance</a>.  Read more under <a href="https://github.com/sappenin/objectify-utils/blob/master/README-Translators.md">Enhanced Translators</a>.

+ <b>Enhanced Annotations and Translators for Joda "Money" Types</b><br/>
Store entities with Joda-Money properties in interesting ways, fully controllable via field annotations.  Read more under <a href="https://github.com/sappenin/objectify-utils/blob/master/README-Translators.md">Enhanced Translators</a>.

+ <b>High Throughput ShardedCounter Implementation</b><br/>
Allow for high-throughput counters backed by the HRD datastore.  Read more under <a href="https://github.com/sappenin/objectify-utils/blob/master/README-ShardedCounter.md">ShardedCounter Service</a>.

<b><i><u>Note: The current release of this library is not compatible with Objectify versions prior to version 4.0rc1.  See the changelog for previous version support.</u></i></b>

Getting Started
----------

First, download the latest <a href="https://github.com/sappenin/objectify-utils/archive/2.2.1.zip">objectify-utils-2.2.1.jar</a> and include it your application's classpath.

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
		<version>2.2.1</version>
    </dependency>

Next, be sure to register any annotations that you plan to use, as follows:

	ObjectifyService.factory().getTranslators().add(new BigDecimalStringTranslatorFactory());
	ObjectifyService.factory().getTranslators().add(new JodaMoneyTranslatorFactory());
	ObjectifyService.factory().getTranslators().add(new UTCReadableInstantTranslatorFactory());

Change Log
----------
**Version 2.2.1**
+ Added missing #build to ShardedCounterServiceConfiguration.Builder
+ Updated to Appengine: 1.8.1 (provided)

**Version 2.2.0**
+ Updated to Appengine: 1.8.0 (provided)
+ Updated to Objectify: 4.0rc1 (provided)
+ Now compiled with Java 7

**Version 2.1.0**
+ Updated to Appengine: 1.7.6 (provided)
+ Updated to Objectify: 4.0b2 (provided)
+ Updated to Joda Time: 2.2
+ Updated to Joda Money: 0.8
+ Updated to Guava: 14.0.1

**Version 2.0.0**
+ Updated to Objectify 4.0

Authors
-------

**David Fuelling**

+ sappenin@gmail.com
+ http://twitter.com/sappenin
+ http://github.com/sappenin


Copyright and License
---------------------

Copyright 2013 Sappenin Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.