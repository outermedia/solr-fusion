package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * The *:* query.
 */
@ToString(callSuper = true)
public class MatchAllDocsQuery extends Query
{
    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
