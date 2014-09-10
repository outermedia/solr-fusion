package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data holder class which stores the global search server configuration and all search server specific settings.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalSearchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/",
    propOrder = {
        "timeout", "disasterLimit", "disasterMessage", "defaultPageSize", "queryParserFactory",
        "dismaxQueryParserFactory", "defaultResponseParserFactory", "responseRendererFactories", "queryBuilderFactory",
        "dismaxQueryBuilderFactory", "merge", "searchServerConfigs"
    })
@Getter
@Setter
@ToString
@Slf4j
public class GlobalSearchServerConfig
{
    @XmlElement(name = "timeout", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int timeout;

    @XmlElement(name = "disaster-limit", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int disasterLimit;

    @XmlElement(name = "error", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private Message disasterMessage;

    @XmlElement(name = "page-size", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int defaultPageSize;

    @XmlElement(name = "query-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private QueryParserFactory queryParserFactory;

    @XmlElement(name = "dismax-query-parser", namespace = "http://solrfusion.outermedia.org/configuration/",
        required = true)
    private QueryParserFactory dismaxQueryParserFactory;

    @XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/",
        required = true)
    private ResponseParserFactory defaultResponseParserFactory;

    @XmlElement(name = "response-renderer", namespace = "http://solrfusion.outermedia.org/configuration/",
        required = true)
    private List<ResponseRendererFactory> responseRendererFactories;

    @XmlElement(name = "query-builder", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private QueryBuilderFactory queryBuilderFactory;

    @XmlElement(name = "dismax-query-builder", namespace = "http://solrfusion.outermedia.org/configuration/",
        required = true)
    private QueryBuilderFactory dismaxQueryBuilderFactory;

    @XmlElement(name = "merge", namespace = "http://solrfusion.outermedia.org/configuration/", required = false)
    private Merge merge;

    @XmlElement(name = "solr-server", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<SearchServerConfig> searchServerConfigs;

    /**
     * Find a response renderer by type.
     *
     * @param type is either PHP, JSON or XML (see {@link ResponseRendererType})
     * @return null for an error or an instance of {@link ResponseRendererIfc}
     */
    public ResponseRendererIfc getResponseRendererByType(ResponseRendererType type)
        throws InvocationTargetException, IllegalAccessException
    {
        ResponseRendererIfc result = null;
        if (responseRendererFactories != null && type != null)
        {
            for (ResponseRendererFactory rr : responseRendererFactories)
            {
                if (rr.getType().equals(type))
                {
                    result = rr.getInstance();
                }
            }
        }
        return result;
    }

    /**
     * Get the query parser instance.
     *
     * @return an instance of QueryParserIfc
     */
    public QueryParserIfc getQueryParser() throws InvocationTargetException, IllegalAccessException
    {
        return queryParserFactory.getInstance();
    }

    /**
     * Get the default response parser. Please note that {@link SearchServerAdapterIfc} can use a special response
     * parser.
     *
     * @return an instance of ResponseParserIfc.
     */
    public ResponseParserIfc getDefaultResponseParser() throws InvocationTargetException, IllegalAccessException
    {
        return defaultResponseParserFactory.getInstance();
    }

    /**
     * Get the configured query builder.
     *
     * @return a non null instance of QueryBuilderIfc
     */
    public QueryBuilderIfc getDefaultQueryBuilder() throws InvocationTargetException, IllegalAccessException
    {
        return queryBuilderFactory.getInstance();
    }

    /**
     * Get the optionally declared merge strategy.
     *
     * @return an instance of MergeStrategyIfc
     */
    public MergeStrategyIfc getMerger() throws InvocationTargetException, IllegalAccessException
    {
        if (merge == null)
        {
            return null;
        }
        return merge.getInstance();
    }

    /**
     * Get all configured search servers. Every call of this method returns a new list object, but the {@link
     * SearchServerAdapterIfc} instances are re-used.
     *
     * @return a list of SearchServerAdapterIfc
     */
    public List<SearchServerAdapterIfc> getSearchServers() throws InvocationTargetException, IllegalAccessException
    {
        List<SearchServerAdapterIfc> result = new ArrayList<>();
        if (searchServerConfigs != null)
        {
            for (SearchServerConfig config : searchServerConfigs)
            {
                result.add(config.getInstance());
            }
        }
        return result;
    }

    /**
     * Get a search server's configuration by the server's name.
     *
     * @param searchServerId
     * @return null or an object
     */
    public SearchServerConfig getSearchServerConfigById(String searchServerId)
    {
        SearchServerConfig result = null;
        if (searchServerConfigs != null)
        {
            for (SearchServerConfig config : searchServerConfigs)
            {
                if (searchServerId.equals(config.getSearchServerName()))
                {
                    result = config;
                    break;
                }
            }
        }
        return result;
    }

    public QueryBuilderIfc getDismaxQueryBuilder() throws InvocationTargetException, IllegalAccessException
    {
        return dismaxQueryBuilderFactory.getInstance();
    }

    protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        if (searchServerConfigs != null)
        {
            for (int i = searchServerConfigs.size() - 1; i >= 0; i--)
            {
                SearchServerConfig searchServerConfig = searchServerConfigs.get(i);
                if (!searchServerConfig.getEnabled())
                {
                    log.info("Removed disabled search server config '{}'", searchServerConfig.getSearchServerName());
                    searchServerConfigs.remove(i);
                }
            }
        }
    }

    public QueryParserIfc getDismaxQueryParser() throws InvocationTargetException, IllegalAccessException
    {
        return dismaxQueryParserFactory.getInstance();
    }
}
