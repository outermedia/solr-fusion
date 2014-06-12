package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Data holder class to store the response renderer factory's class
 * configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseRenderer", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder = {"factoryConfig"})
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseRendererFactory extends
	ConfiguredFactory<ResponseRendererIfc, ResponseRendererFactory>
{
	@XmlAttribute(name = "type", required = true)
	private ResponseRendererType type;

    @XmlAnyElement
    private List<Element> factoryConfig;
}
