package org.outermedia.solrfusion.configuration;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Abstract data holder for factory setting configurations. This is the parent
 * class of all configurations which have a "class" attribute.
 *
 * @author ballmann
 */

@XmlTransient
@Getter
@Setter
@ToString(exclude = {"getInstanceMethod"})
@Slf4j
public abstract class ConfiguredFactory<T extends Initiable<C>, C>
{
    @XmlAttribute(name = "class", required = true)
    private String classFactory;

    @XmlTransient
    private Class<T> typeClassFactory;

    @XmlTransient
    private Method getInstanceMethod;

    /**
     * Hook up unmarshalling in order to create an instance of
     * {@link ConfiguredFactory#classFactory}.
     *
     * @param u      is the unmarshaller
     * @param parent the parent object
     * @throws UnmarshalException is thrown when exceptions occur (e.g. configured class not found)
     */
    @SuppressWarnings("unchecked")
    protected void afterUnmarshal(Unmarshaller u, Object parent)
            throws UnmarshalException
    {
        if (classFactory != null)
        {
            try
            {
                typeClassFactory = (Class<T>) Class.forName(classFactory);
                getInstanceMethod = typeClassFactory
                        .getMethod("getInstance");
                if (getInstanceMethod == null)
                {
                    throw new RuntimeException(
                            "Didn't find method public static T getInstance() in class "
                                    + typeClassFactory.getName());
                }
                if (!Modifier.isStatic(getInstanceMethod.getModifiers()))
                {
                    getInstanceMethod = null;
                    throw new RuntimeException(
                            "Expected to find static method getInstance() in class "
                                    + typeClassFactory.getName()
                                    + ", but found instance method.");
                }
            }
            catch (Exception e)
            {
                getInstanceMethod = null;
                log.error("Caught exception while creating instance {}",
                        classFactory, e);
                throw new UnmarshalException("Post processing of xml unmarshalling failed", e);
            }
        }
        else
        {
            log.error("Attribute 'class' has no value or couldn't be parsed for {}", getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public T getInstance() throws IllegalAccessException, InvocationTargetException
    {
        T result = null;
        if (getInstanceMethod != null)
        {
            try
            {
                result = (T) getInstanceMethod.invoke(null);
                result.init((C) this);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                log.error("Caught exception while creating instance {}",
                        typeClassFactory.getName(), e);
                throw e;
            }
        }
        else
        {
            log.error("Attribute 'class' has no value or couldn't be parsed for "
                    + getClass().getName());
        }
        return result;
    }
}
