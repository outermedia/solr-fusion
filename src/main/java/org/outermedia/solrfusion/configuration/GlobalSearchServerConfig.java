package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data holder class keeping the global search server configuration and all
 * search server specific settings.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalSearchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "timeout", "disasterLimit", "disasterMessage", "queryParserFactory",
                "defaultResponseParserFactory", "responseRendererFactories", "queryBuilderFactory", "merge",
                "searchServerConfigs"
        })
@Getter
@Setter
@ToString
public class GlobalSearchServerConfig
{
    @XmlElement(name = "timeout", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int timeout;

    @XmlElement(name = "disaster-limit", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int disasterLimit;

    @XmlElement(name = "error", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private Message disasterMessage;

    @XmlElement(name = "query-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private QueryParserFactory queryParserFactory;

    @XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private ResponseParserFactory defaultResponseParserFactory;

    @XmlElement(name = "response-renderer", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<ResponseRendererFactory> responseRendererFactories;

    @XmlElement(name = "query-builder", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private QueryBuilderFactory queryBuilderFactory;

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
    public ResponseRendererIfc getResponseRendererByType(
            ResponseRendererType type) throws InvocationTargetException, IllegalAccessException
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
     * Get the default response parser. Please note that
     * {@link SearchServerAdapterIfc} can use a special response parser.
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
        return merge.getInstance();
    }

    /**
     * Get all configured search servers. Every call of this method returns a
     * new list object, but the {@link SearchServerAdapterIfc} instances are
     * re-used.
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
}
