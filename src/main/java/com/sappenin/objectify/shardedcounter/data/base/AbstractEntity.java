package com.sappenin.objectify.shardedcounter.data.base;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Id;

/**
 * An abstract base class for all sappenin-objectify-utils entities (data stored
 * to the datastore).
 * 
 * @author david
 * 
 */
@Getter
@Setter
public abstract class AbstractEntity
{
	@Id
	private String id;

	private DateTime creationDateTime;
	private DateTime updatedDateTime;

	/**
	 * Default Constructor
	 */
	public AbstractEntity()
	{
		this(UUID.randomUUID().toString());
	}

	/**
	 * Required Params constructor
	 * 
	 * @param id A globally unique identifier (i.e., a {@link UUID} as a
	 *            String).
	 */
	public AbstractEntity(String id)
	{
		this.id = id;
		this.creationDateTime = DateTime.now(DateTimeZone.UTC);
		this.updatedDateTime = DateTime.now(DateTimeZone.UTC);
	}

	/**
	 * By default, Entities have a null parent Key. This is overridden by
	 * implementations if a Parent key exists.
	 */
	public Key<?> getParentKey()
	{
		return null;
	}

	/**
	 * Assembles the Key for this entity. If an Entity has a Parent Key, that
	 * key will be included in the returned Key heirarchy.
	 * 
	 * @return
	 */
	public <T> Key<T> getTypedKey()
	{
		if (this.getId() == null)
		{
			return null;
		}
		else
		{
			return ObjectifyService.factory().getKey(this);
			//return Key.<T> create(getParentKey(), getClass(), this.getId());
		}
	}

	/**
	 * Assembles the Key for this entity. If an Entity has a Parent Key, that
	 * key will be included in the returned Key heirarchy.
	 */
	public com.google.appengine.api.datastore.Key getKey()
	{
		Key<?> typedKey = this.getTypedKey();
		return typedKey == null ? null : typedKey.getRaw();
	}

}
