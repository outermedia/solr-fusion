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

import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;

/**
 * Data holder class keeping the fusion configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{
	"fusionFields", "scriptTypes", "defaultSearchField", "defaultOperator",
	"idGeneratorFactory", "searchServerConfigs"
})
@XmlRootElement(name = "core", namespace = "http://solrfusion.outermedia.org/configuration/")
@ToString
public class Configuration
{
	@XmlElementWrapper(name = "fusion-schema-fields", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<FusionField> fusionFields;

	@XmlElement(name = "script-type", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@Getter
	@Setter
	private List<ScriptType> scriptTypes;

	@XmlElement(name = "default-search-field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@Getter
	@Setter
	private String defaultSearchField;

	@XmlElement(name = "default-operator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@Getter
	@Setter
	private String defaultOperator;

	@XmlElement(name = "id-generator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@Getter
	@Setter
	private IdGeneratorFactory idGeneratorFactory;

	@XmlElement(name = "solr-servers", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	@Getter
	@Setter
	private GlobalSearchServerConfig searchServerConfigs;

	/**
	 * Find a response renderer by type.
	 * 
	 * @param type is either PHP, JSON or XML (see {@link ResponseRendererType})
	 * @return null for an error or an instance of {@link ResponseRendererIfc}
	 */
	public ResponseRendererIfc getResponseRendererByType(
		ResponseRendererType type)
	{
		return searchServerConfigs.getResponseRendererByType(type);
	}

	/**
	 * Get the response timeout in seconds.
	 * 
	 * @return a number greater 0.
	 */
	public int getTimeout()
	{
		return searchServerConfigs.getTimeout();
	}

	/**
	 * The minimal number of search servers which have to respond. If too few
	 * respond the disaster message (see {@link #getDisasterMessage()}) is
	 * thrown.
	 * 
	 * @return the minimal number of responding servers
	 */
	public int getDisasterLimit()
	{
		return searchServerConfigs.getDisasterLimit();
	}

	/**
	 * Get the disaster message (see {@link #getDisasterLimit()}) when too few
	 * server respond.
	 * 
	 * @return a Message object which contains a key and the text
	 */
	public Message getDisasterMessage()
	{
		return searchServerConfigs.getDisasterMessage();
	}

	/**
	 * Get the query parser instance.
	 * 
	 * @return an instance of QueryParserIfc
	 */
	public QueryParserIfc getQueryParser()
	{
		return searchServerConfigs.getQueryParser();
	}

	/**
	 * Get the default response parser. Please note that
	 * {@link SearchServerAdapterIfc} can use a special response parser.
	 * 
	 * @return an instance of ResponseParserIfc.
	 */
	public ResponseParserIfc getDefaultResponseParser()
	{
		return searchServerConfigs.getDefaultResponseParser();
	}

	/**
	 * Get the optionally declared merge strategy.
	 * 
	 * @return an instance of MergeStrategyIfc
	 */
	public MergeStrategyIfc getMerger()
	{
		return searchServerConfigs.getMerger();
	}

	/**
	 * Get all configured search servers. Every call of this method returns a
	 * new list object, but the {@link SearchServerAdapterIfc} instances are
	 * re-used.
	 * 
	 * @return a list of SearchServerAdapterIfc
	 */
	public List<SearchServerAdapterIfc> getSearchServers()
	{
		return searchServerConfigs.getSearchServers();
	}

	public FusionField findFieldByName(String name)
	{
		for (FusionField ff : fusionFields)
		{
			if (ff.getFieldName().equals(name)) return ff;
		}
		return null;
	}
}
