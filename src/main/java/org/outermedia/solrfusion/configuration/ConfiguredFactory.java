package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract data holder for factory settings.
 * 
 * @author ballmann
 * 
 */

@XmlTransient
@Getter
@Setter
@ToString
public abstract class ConfiguredFactory
{
	@XmlAttribute(name = "class", required = true)
	private String classFactory;
}
