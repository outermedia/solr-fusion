package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class keeping the configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{
	"fusionFields", "scriptTypes", "defaultSearchField", "defaultOperator",
	"idGeneratorFactory", "searchServers"
})
@XmlRootElement(name = "core", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString
public class Configuration
{
	@XmlElementWrapper(name = "fusion-schema-fields", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<FusionField> fusionFields;

	@XmlElement(name = "script-type", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<ScriptType> scriptTypes;

	@XmlElement(name = "default-search-field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private String defaultSearchField;

	@XmlElement(name = "default-operator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private String defaultOperator;

	@XmlElement(name = "id-generator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private IdGeneratorFactory idGeneratorFactory;

	@XmlElement(name = "solr-servers", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private GlobalSearchServerConfig searchServers;
}
