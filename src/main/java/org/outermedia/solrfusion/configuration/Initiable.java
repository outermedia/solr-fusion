package org.outermedia.solrfusion.configuration;

import java.lang.reflect.InvocationTargetException;

/**
 * All {@link ConfiguredFactory#getClass()} implementors have to implement this
 * interface.
 * 
 * @author ballmann
 * 
 * @param <T>
 */
public interface Initiable<T>
{
	public void init(T config) throws InvocationTargetException, IllegalAccessException;
}
