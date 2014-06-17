package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerQueryBuilderFactory;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.Query;

/**
 * Created by ballmann on 6/17/14.
 */
public interface SearchServerQueryBuilderIfc extends QueryVisitor, Initiable<SearchServerQueryBuilderFactory>
{
    public String buildQueryString(Query query);
}
