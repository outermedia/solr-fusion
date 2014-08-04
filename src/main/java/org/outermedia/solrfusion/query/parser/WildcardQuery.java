package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
public class WildcardQuery extends TermQuery
{

    public WildcardQuery(Term t)
    {
        super(t);
    }

    protected WildcardQuery() {}

    @Override
    public WildcardQuery shallowClone()
    {
        return shallowCloneImpl(new WildcardQuery());
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
