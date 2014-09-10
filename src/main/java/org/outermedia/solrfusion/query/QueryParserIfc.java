package org.outermedia.solrfusion.query;

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

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.Query;

import java.util.Locale;
import java.util.Map;

/**
 * Transforms a query into an internal object representation.
 *
 * @author ballmann
 */

public interface QueryParserIfc extends Initiable<QueryParserFactory>
{
    /**
     * Parse a fusion query string to an internal representation.
     *
     * @param config               the whole fusion schema
     * @param boosts               boost values for fields (not used)
     * @param query                the fusion query string
     * @param allTermsAreProcessed optional; if set and true, all created Term objects are set to processed/was mapped.
     * @return null when parsing fails otherwise an object
     */
    public Query parse(Configuration config, Map<String, Float> boosts, String query, Locale locale,
        Boolean allTermsAreProcessed) throws Exception;
}
