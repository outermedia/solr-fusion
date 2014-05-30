package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class to store one field mapping configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldMapping", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"operations"
})
@Getter
@Setter
@ToString
public class FieldMapping
{
	@XmlAttribute(name = "name")
	private String searchServersName;

	@XmlAttribute(name = "fusion-name")
	private String fusionName;

	@XmlElements(value =
	{
		@XmlElement(name = "add", type = AddOperation.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
		@XmlElement(name = "drop", type = DropOperation.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
		@XmlElement(name = "change", type = ChangeOperation.class, namespace = "http://solrfusion.outermedia.org/configuration/")
	})
	private List<Operation> operations;
}
