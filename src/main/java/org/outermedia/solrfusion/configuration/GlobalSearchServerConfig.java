package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class keeping the global search server configuration and all
 * search server specific settings.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalSearchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"timeout", "desasterLimit", "desasterMessage", "queryParserFactory",
	"defaultResponseParserFactory", "responseRendererFactories",
	"searchServers"
})
@Getter
@Setter
@ToString
public class GlobalSearchServerConfig
{
	@XmlElement(name = "timeout", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private int timeout;

	@XmlElement(name = "desaster-limit", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private int desasterLimit;

	@XmlElement(name = "error", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private String desasterMessage;

	@XmlElement(name = "query-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private QueryParserFactory queryParserFactory;

	@XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private ResponseParserFactory defaultResponseParserFactory;

	@XmlElement(name = "response-renderer", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<ResponseRendererFactory> responseRendererFactories;

	/*
	 TODO <merge>
	 */

	@XmlElement(name = "solr-server", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<SearchServerConfig> searchServers;
}
