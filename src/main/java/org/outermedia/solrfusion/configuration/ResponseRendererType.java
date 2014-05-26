package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The three supported response renderer types.
 * 
 * @author ballmann
 * 
 */

@XmlType
@XmlEnum(String.class)
public enum ResponseRendererType
{
	@XmlEnumValue("xml")
	XML, @XmlEnumValue("json")
	JSON, @XmlEnumValue("php")
	PHP,
}