package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;

/**
 * Data holder keeping one search server's configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"url", "scoreFactory", "responseParserFactory", "idFieldName",
	"fieldMappings"
})
@Getter
@Setter
@ToString(callSuper = true)
public class SearchServerConfig extends
	ConfiguredFactory<SearchServerAdapterIfc, SearchServerConfig>
{
	@XmlAttribute(name = "name", required = true)
	private String searchServerName;

	@XmlAttribute(name = "version", required = true)
	private String searchServerVersion;

	@XmlElement(name = "url", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private String url;

	@XmlElement(name = "score", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private ScoreFactory scoreFactory;

	@XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = false)
	private ResponseParserFactory responseParserFactory;

	@XmlElement(name = "unique-key", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private String idFieldName;

	@XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<FieldMapping> fieldMappings;

}
