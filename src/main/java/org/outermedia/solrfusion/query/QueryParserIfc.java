package org.outermedia.solrfusion.query;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.ParseException;
import org.outermedia.solrfusion.query.parser.Query;

import java.util.Locale;
import java.util.Map;

/**
 * Transforms a query into an internal representation.
 *
 * @author ballmann
 */

public interface QueryParserIfc extends Initiable<QueryParserFactory>
{
    /**
     * Parse a fusion query string to an internal representation.
     *
     * @param config               the whole fusion schema
     * @param boosts               boost values for fields
     * @param query                the fusion query string
     * @param allTermsAreProcessed optional; if set and true, all created Term objects are set to processed/wasmapped.
     * @return null when parsing fails otherwise an object
     */
    public Query parse(Configuration config, Map<String, Float> boosts, String query, Locale locale,
        Boolean allTermsAreProcessed) throws ParseException;
}
