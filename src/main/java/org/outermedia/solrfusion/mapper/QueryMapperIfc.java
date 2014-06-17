package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryMapperFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Transforms a query into an internal representation.
 *
 * @author ballmann
 */

public interface QueryMapperIfc extends Initiable<QueryMapperFactory>
{
    /**
     * Map a query to a certain search server (serverConfig).
     *
     * @param serverConfig the currently used server's configuration
     * @param query        the query to map to process
     * @param env          the environment needed by the scripts which transform values
     */
    public void mapQuery(SearchServerConfig serverConfig, Query query, ScriptEnv env);
}
