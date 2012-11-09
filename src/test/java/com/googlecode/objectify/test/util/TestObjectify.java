package com.googlecode.objectify.test.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.util.cmd.ObjectifyWrapper;

/**
 * Adds some convenience methods. Most of the tests were written against
 * Objectify 3 and it's a PITA to convert all the calls.
 */
public class TestObjectify extends ObjectifyWrapper<TestObjectify, ObjectifyFactory>
{

	/** */
	public TestObjectify(Objectify ofy)
	{
		super(ofy);
	}

	public <K> K get(Key<K> key)
	{
		return this.load().key(key).get();
	}
}