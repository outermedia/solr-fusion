package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Apply the configured mappings to a query object.
 *
 * @author ballmann
 */

public interface QueryMapperIfc extends Initiable<QueryMapperFactory>
{
    /**
     * Map a query to a specific Solr search server (serverConfig).
     * @param config       the fusion schema
     * @param serverConfig the currently used server's configuration
     * @param query        the query to map
     * @param env          the environment needed by the scripts which transform values
     * @param fusionRequest the current SolrFusion request
     * @param target        for which request part this query mapper is called
     */
    public void mapQuery(Configuration config, SearchServerConfig serverConfig, Query query, ScriptEnv env,
        FusionRequest fusionRequest, QueryTarget target);
}
