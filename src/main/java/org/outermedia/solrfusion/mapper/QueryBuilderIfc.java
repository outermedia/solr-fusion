package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryBuilderFactory;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.Query;

/**
 * Created by ballmann on 6/17/14.
 */
public interface QueryBuilderIfc extends QueryVisitor, Initiable<QueryBuilderFactory>
{
    public String buildQueryString(Query query, Configuration configuration);
}
