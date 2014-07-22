package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryBuilderFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.Query;

import java.util.Locale;

/**
 * Created by ballmann on 6/17/14.
 */
public interface QueryBuilderIfc extends QueryVisitor, Initiable<QueryBuilderFactory>
{
    /**
     * Builds a query string which contains the string of query and queries added by a &lt;om:add&gt; mapping.
     * @param query
     * @param configuration
     * @param searchServerConfig
     * @param locale
     * @return
     */
    public String buildQueryString(Query query, Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale);

    /**
     * Builds a query string which contains the string of query but &lt;om:add&gt; mappings are ignored.
     * @param query
     * @param configuration
     * @param searchServerConfig
     * @param locale
     */
    public String buildQueryStringWithoutNew(Query query, Configuration configuration,
        SearchServerConfig searchServerConfig, Locale locale);
}
