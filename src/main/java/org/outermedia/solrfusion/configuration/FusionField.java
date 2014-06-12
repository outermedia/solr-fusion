package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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

	@XmlAttribute(name = "name", required = true)
	private String fieldName;

	@XmlAttribute(name = "type", required = false)
	private String type = "text";

	@XmlAttribute(name = "format", required = false)
	private String format;

	/**
	 * Get the {@link #type}'s corresponding enum.
	 * 
	 * @return null for unknown or an instance
	 */
	public DefaultFieldType getFieldType()
	{
		DefaultFieldType result = null;
		try
		{
			result = DefaultFieldType.valueOf(type.toUpperCase());
		}
		catch (Exception e)
		{
			// NOP
		}
		return result;
	}
}
