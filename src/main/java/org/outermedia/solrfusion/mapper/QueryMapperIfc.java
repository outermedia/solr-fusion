package org.outermedia.solrfusion.mapper;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
