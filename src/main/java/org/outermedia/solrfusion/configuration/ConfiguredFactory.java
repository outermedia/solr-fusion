package org.outermedia.solrfusion.configuration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract data holder for factory setting configurations. This is the parent
 * class of all configurations which have a "class" attribute.
 * 
 * @author ballmann
 * 
 */

@XmlTransient
@Getter
@Setter
@ToString
@Slf4j
public abstract class ConfiguredFactory<T extends Initiable<C>, C>
{
	@XmlAttribute(name = "class", required = true)
	private String classFactory;

	@XmlTransient
	private T implementation;

	/**
	 * Hook up unmarshalling in order to create an instance of
	 * {@link ConfiguredFactory#classFactory}.
	 * 
	 * @param u is the unmarshaller
	 * @param parent the parent object
	 */
	@SuppressWarnings("unchecked")
	protected void afterUnmarshal(Unmarshaller u, Object parent)
	{
		if (classFactory != null)
		{
			try
			{
				Class<?> typeClassFactory = Class.forName(classFactory);
				Method getInstanceMethod = typeClassFactory
					.getMethod("getInstance");
				if (getInstanceMethod == null)
				{
					throw new RuntimeException(
						"Didn't find method public static T getInstance() in class "
							+ typeClassFactory.getName());
				}
				if (!Modifier.isStatic(getInstanceMethod.getModifiers()))
				{
					throw new RuntimeException(
						"Expected to find static method getInstance() in class "
							+ typeClassFactory.getName()
							+ ", but found instance method.");
				}
				if (implementation != null)
				{
					throw new RuntimeException("Found already created object");
				}
				implementation = (T) getInstanceMethod.invoke(null);
				implementation.init((C) this);
			}
			catch (Exception e)
			{
				log.error("Caught exception while creating instance {}",
					classFactory, e);
			}
		}
		else
		{
			log.error("Attribute 'class' has no value or couldn't be parsed for "
				+ getClass().getName());
		}
	}
}
