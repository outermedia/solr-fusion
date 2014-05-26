package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class to store the response renderer factory's class.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseRenderer", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseRendererFactory extends ConfiguredFactory
{
	@XmlAttribute(name = "type", required = true)
	private ResponseRendererType fusionFieldName;
}
