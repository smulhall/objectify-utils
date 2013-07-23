High Throughput ShardedCounter
===========================

Objectify-Utils includes a ShardedCounter implementation that utilizes Objectify 4.  The code is patterned off of the following <a href="https://developers.google.com/appengine/articles/sharding_counters">article</a> from developer.google.com, and the the rationale for a ShardedCounter is as follows (quoted from the linked article):

> When developing an efficient application on Google App Engine, you need to pay attention to how often an entity is updated. While App Engine's datastore scales to support a huge number of entities, it is important to note that you can only expect to update any single entity or entity group about five times a second. That is an estimate and the actual update rate for an entity is dependent on several attributes of the entity, including how many properties it has, how large it is, and how many indexes need updating. While a single entity or entity group has a limit on how quickly it can be updated, App Engine excels at handling many parallel requests distributed across distinct entities, and we can take advantage of this by using sharding."

Thus, when a datastore-backed counter is required (i.e., for counter consistency, redundancy, and availability) we can increment random Counter shards in parallel and achieve a high-throughput counter without sacrificing consistency or availability.  For example, if a particular counter needs to support 100 increments per second, then the application supporting this counter could create the counter with approximately 20 shards, and the throughput could be sustained.


Features
------
+ <b>High Throughput Counter Increment/Decrement</b><br/>
Increment and Decrement across N shards for high scalability.

+ <b>Async Counter Deletion</b><br/>
Sharded counters with large numbers of CounterShards can take some time to delete.  Thus, counter deletion occurs inside of a Task Queue job to avoid timeouts.  In the future, this will be made optional (e.g., for counters that have a small number of counter shards).

Getting Started
----------
Sharded counters can be accessed via an implementation of <a href="">CounterService</a>.  Currently, the only implementation is <a href="">ShardedCounterService<a/>, which requires a TaskQueue (the "/default" queue is used by default) if Counter deletion is required.

Please see below for confiugration examples using Spring and Guice, respectively.

Queue Configuration
----------
 	<queue-entries>
 		<queue>
			<name>deleteCounterShardQueue</name>
			<!-- add any further queue configuration here -->
		</queue>
	</queue-entries>

Don't forget to add a URL mapping for the default queue, or for the queue mapping you specify below!  By default, the ShardedCounterService uses the default queue URL.  See <a href="https://developers.google.com/appengine/docs/java/taskqueue/overview-push#URL_Endpoints">here</a> for how to configure your push queue URL endpoints.

<i><b>Note that this queue is not required if Counter deletion will not be utilized by your application</b></i>.

Objectify Entity Registration
-----------
Next, be sure to register the entities that are required by the CounterService, as follows:

	ObjectifyService.factory().getTranslators().add(new Counter());
	ObjectifyService.factory().getTranslators().add(new CounterShard());

Default Service Setup using Spring
-------
To utilize the ShardedCounterService with Spring, the following will initialize the service with a default configuration:

	<bean id="shardedCounterService"
		class="com.sappenin.objectify.shardedcounter.service.ShardedCounterService">
	</bean>

Custom Service Configuration using Spring
-------
If you want to control the configuration of the ShardedCounterService, you will need to configure an instance of <b>ShardedCounterServiceConfiguration.Builder</b> as follows:

	<bean id="shardedCounterServiceConfigurationBuilder"
		class="com.sappenin.objectify.shardedcounter.service.ShardedCounterServiceConfiguration.Builder">

		<!-- The number of shards to create when a new counter is created -->
		<property name="numInitialShards">
			<value>1</value>
		</property>

		<!-- The name of the Queue for counter-deletion.  If this property is omitted, the default appengine queue is used -->
		<property name="deleteCounterShardQueueName">
			<value>deleteCounterShardQueue</value>
		</property>

		<!-- The URL callback path that appengine will use to process delete-counter message.  If this property is ommitted, the default appengine queue is used -->
		<property name="relativeUrlPathForDeleteTaskQueue">
			<value>/_ah/queue/deleteCounterShardQueue</value>
		</property>

	</bean>

Next, use the builder defined above to populate a <b>ShardedCounterServiceConfiguration</b>:

	<bean id="shardedCounterServiceConfiguration"
		class="com.sappenin.objectify.shardedcounter.service.ShardedCounterServiceConfiguration">

		<constructor-arg>
			<ref bean="shardedCounterServiceConfigurationBuilder" />
		</constructor-arg>

	</bean>

Finally, use the configuration defined above to create a <b>ShardedCounterService</b> bean.  Notice that you will also need to provide a spring-bean for the memcache service:

	<bean id="memcacheService" 
		class="com.google.appengine.api.memcache.MemcacheServiceFactory"
		factory-method="getMemcacheService">
	</bean>

	<bean id="shardedCounterService"
		class="com.sappenin.objectify.shardedcounter.service.ShardedCounterService">

		<constructor-arg>
			<ref bean="memcacheService" />
		</constructor-arg>

		<constructor-arg>
			<ref bean="shardedCounterServiceConfiguration" />
		</constructor-arg>

	</bean>



Default Guice Setup using Guice Annotations
-------
To utilize the a default configuration of the <b>ShardedCounterService</b> with Guice, add the following methods to one of your Guice modules:

	@Provides
	@RequestScoped
	public Ofy provideOfy(OfyFactory fact)
	{
		return fact.begin();
	}

	@Provides
	@RequestScoped
	public MemcacheService provideMemcacheService()
	{
		return MemcacheServiceFactory.getMemcacheService();
	}

	// The entire app can have a single ShardedCounterServiceConfiguration, though making
	// this request-scoped would allow the config to vary per-request
	@Provides
	@Singleton
	public ShardedCounterServiceConfiguration provideShardedCounterServiceCoonfiguration(OfyFactory ofyFactory,
			MemcacheService memcacheService)
	{
		return new ShardedCounterServiceConfiguration.Builder().withNumInitialShards(2).build();
	}

	// Be safe and make this RequestScoped though not technically needed to be RequestScoped 
	// since ShardedCounterServiceConfiguration and MemcacheService are immutable or utilize 
	// thread-local internally.
	@Provides
	@RequestScoped
	public ShardedCounterService provideShardedCounterService(MemcacheService memcacheService,
			ShardedCounterServiceConfiguration config)
	{
		return new ShardedCounterService(memcacheService, config);
	}

Don't forget to wire Objectify into Guice:

	public class OfyFactory extends ObjectifyFactory
	{
		/** Register our entity types */
		public OfyFactory()
		{
			final long registrationStartTime = System.currentTimeMillis();

			// ///////////////////
			// Translation Classes
			// ///////////////////

			final com.sappenin.objectify.translate.BigDecimalStringTranslatorFactory bigDecimalStringTranslatorFactory = new BigDecimalStringTranslatorFactory();
			getTranslators().add(bigDecimalStringTranslatorFactory);

			final com.sappenin.objectify.translate.JodaMoneyTranslatorFactory jodaMoneyTranslatorFactory = new JodaMoneyTranslatorFactory();
			getTranslators().add(jodaMoneyTranslatorFactory);

			final com.sappenin.objectify.translate.UTCReadableInstantTranslatorFactory utcReadableInstantTranslatorFactory = new UTCReadableInstantTranslatorFactory();
			getTranslators().add(utcReadableInstantTranslatorFactory);

			// ///////////////////
			// Register Entities
			// ///////////////////

			// ShardedCounter Entities
			register(Counter.class);
			register(CounterShard.class);

			OfyFactory.logger.info("Objectify Class Registration took "
				+ (System.currentTimeMillis() - registrationStartTime) + " millis");
		}

		@Override
		public Ofy begin()
		{
			return new Ofy(this);
		}
	}

For a more complete example of wiring Guice and Objectify, see the <a href="https://github.com/stickfigure/motomapia">Motomapia Source</a>. 


Default Guice Setup without Annotations
-------
To utilize the default configuration of <b>ShardedCounterService</b> with Guice (without using Guice Annotations), add the following classes to your project to create Providers for the ShardedCounterServiceConfiguration, MemcacheService, and ShardedCounterService:	

	public class MemcacheServiceProvider implements Provider<MemcacheService>
	{
		@Override
		public ShardedCounterService get()
		{
			return MemcacheServiceFactory.getMemcacheService();
		}
	}

	public class ShardedCounterServiceConfigurationProvider implements
			Provider<ShardedCounterServiceConfiguration>
	{
		@Override
		public ShardedCounterServiceConfiguration get()
		{
			return new ShardedCounterServiceConfiguration.Builder().withNumInitialShards(2).build();
		}
	}

	public class ShardedCounterServiceProvider implements Provider<ShardedCounterService>
	{
		private final ShardedCounterServiceConfiguration config;
		private final MemcacheService memcacheService;

		/**
		 * Required-args Constructor.
		 * 
		 * @param config
		 * @param memcacheService
		 */
		@Inject
		public ShardedCounterServiceProvider(final ShardedCounterServiceConfiguration config,
				final MemcacheService memcacheService)
		{
			this.config = config;
			this.memcacheService = memcacheService;
		}

		@Override
		public ShardedCounterService get()
		{
			return new ShardedCounterService(memcacheService, config);
		}
	}

Finally, wire everything together in the configure() method of one of your Guice modules:

	bind(MemcacheService.class).toProvider(MemcacheServiceProvider.class).in(RequestScoped.class);
	// The entire app can have a single ShardedCounterServiceConfiguration, though making
	// this request-scoped would allow the config to vary per-request
	bind(ShardedCounterServiceConfiguration.class).toProvider(ShardedCounterServiceConfigurationProvider.class);
	bind(ShardedCounterService.class).toProvider(ShardedCounterServiceProvider.class).in(RequestScoped.class);
	
	

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