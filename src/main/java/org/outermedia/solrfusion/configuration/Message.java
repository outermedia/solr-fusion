package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder to store a message key and text configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString
public class Message
{
	@XmlAttribute(required = true)
	private String key;

	@XmlValue
	private String text;
}
