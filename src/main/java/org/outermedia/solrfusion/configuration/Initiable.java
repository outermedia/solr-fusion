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
    /**
     * Initialize by the use of the given configuration.
     * @param config the configuration of this class.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
	public void init(T config) throws InvocationTargetException, IllegalAccessException;
}
