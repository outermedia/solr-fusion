package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Data holder class keeping the factory's class and field id configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "response-consolidator", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseConsolidatorFactory extends
        ConfiguredFactory<ResponseConsolidatorIfc, ResponseConsolidatorFactory>
{
}
