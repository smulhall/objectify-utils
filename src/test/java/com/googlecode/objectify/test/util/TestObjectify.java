package com.googlecode.objectify.test.util;

import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.TxnWork;
import com.googlecode.objectify.util.cmd.ObjectifyWrapper;

/**
 * Adds some convenience methods. Most of the tests were written against
 * Objectify 3 and it's a PITA to convert all the calls.
 */
public class TestObjectify extends ObjectifyWrapper<TestObjectify, ObjectifyFactory>
{
	/**
	 * A Work interface you can use with TestObjectify.
	 */
	public interface Work<R> extends TxnWork<TestObjectify, R>
	{
	}

	/** */
	public TestObjectify(Objectify ofy)
	{
		super(ofy);
	}

	public <K, E extends K> Key<K> put(E entitity)
	{
		return this.save().<K, E> entity(entitity).now();
	}

	public <K, E extends K> Map<Key<K>, E> put(E... entities)
	{
		return this.save().<K, E> entities(entities).now();
	}

	public <K> K get(Key<K> key)
	{
		return this.load().key(key).get();
	}
}