package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.Query;

import java.util.Locale;
import java.util.Set;

/**
 * Classes implementing this interface create a Solr query string from an already mapped query object.
 * <p/>
 * Created by ballmann on 6/17/14.
 */
public interface QueryBuilderIfc extends QueryVisitor, Initiable<QueryBuilderFactory>
{
    /**
     * Build the query string for a search server. "outside" non-copying &lt;om:add&gt; mappings are ignored. Usually
     * implementations call {@link #buildQueryStringWithoutNew(org.outermedia.solrfusion.query.parser.Query,
     * org.outermedia.solrfusion.configuration.Configuration, org.outermedia.solrfusion.configuration.SearchServerConfig,
     * java.util.Locale, java.util.Set, org.outermedia.solrfusion.configuration.QueryTarget)} directly.
     *
     * @param query              the query to process
     * @param configuration     the SolrFusion schema
     * @param searchServerConfig    the current destination Solr server configuration
     * @param locale                the localization to use
     * @param defaultSearchServerSearchFields   especially needed in the case that a dismax query shall be built
     * @param target            for which request part this query builder is called
     */
    public String buildQueryString(Query query, Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale, Set<String> defaultSearchServerSearchFields, QueryTarget target);

    /**
     * Builds a query string which contains the string of query but "outside" non-copying &lt;om:add&gt; mappings are
     * ignored.
     *
     * @param query             the query to process
     * @param configuration     the SolrFusion schema
     * @param searchServerConfig    the current destination Solr server configuration
     * @param locale            the localization to use
     * @param defaultSearchServerSearchFields especially needed in the case that a dismax query shall be built
     * @param target            for which request part this query builder is called
     */
    public String buildQueryStringWithoutNew(Query query, Configuration configuration,
        SearchServerConfig searchServerConfig, Locale locale, Set<String> defaultSearchServerSearchFields,
        QueryTarget target);

    /**
     * Get the output of all "outside" non-copying &lt;om:add&gt; mappings.
     * @param configuration     the SolrFusion schema
     * @param searchServerConfig    the current destination Solr server configuration
     * @param locale                the localization to use
     * @param target             for which request part this query builder is called
     * @param result the content of this parameter is prepended to the final result. If empty (not null!) nothing
     *               is prepended.
     * @return a new String
     */
    public String getStaticallyAddedQueries(Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale, QueryTarget target, String result);
}
