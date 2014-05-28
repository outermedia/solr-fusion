package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class keeping the fusion schema field configurations.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldType", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString
public class FusionField
{
	@XmlID
	@XmlAttribute(name = "name", required = true)
	private String fieldName;

	@XmlAttribute(name = "type", required = false)
	private String type = "string";

	@XmlAttribute(name = "format", required = false)
	private String format;
}
