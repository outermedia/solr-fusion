package org.outermedia.solrfusion.query;

import java.util.Map;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.Query;

/**
 * Transforms a query into an internal representation.
 * 
 * @author ballmann
 * 
 */

public interface QueryParserIfc extends Initiable<QueryParserFactory>
{
	public Query parse(Configuration config, Map<String, Float> boosts,
		String query);

	// TODO define required methods e.g. parse
}
