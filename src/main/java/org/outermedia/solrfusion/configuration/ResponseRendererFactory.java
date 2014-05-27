package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.response.ResponseRendererIfc;

/**
 * Data holder class to store the response renderer factory's class
 * configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseRenderer", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseRendererFactory extends
	ConfiguredFactory<ResponseRendererIfc, ResponseRendererFactory>
{
	@XmlAttribute(name = "type", required = true)
	private ResponseRendererType type;
}
