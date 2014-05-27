package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.IdGeneratorIfc;

/**
 * Data holder class keeping the factory's class and field id configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "idGenerator", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString(callSuper = true)
public class IdGeneratorFactory extends
	ConfiguredFactory<IdGeneratorIfc, IdGeneratorFactory>
{
	@XmlAttribute(name = "fusion-name", required = true)
	private String fusionFieldName;
}
