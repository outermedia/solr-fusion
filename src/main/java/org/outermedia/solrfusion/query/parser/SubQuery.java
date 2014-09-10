package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * The _query_:"..." Solr query.
 *
 * Created by ballmann on 8/22/14.
 */
@Getter
@Setter
@ToString
public class SubQuery extends Query
{
    private Query query;

    public SubQuery(Query q)
    {
        query = q;
    }

    @Override public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

}
