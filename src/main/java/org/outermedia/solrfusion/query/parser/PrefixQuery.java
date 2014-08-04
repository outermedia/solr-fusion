package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
public class PrefixQuery extends TermQuery
{

	public PrefixQuery(Term prefix)
	{
		super(prefix);
	}

    protected PrefixQuery() {}

    @Override
    public PrefixQuery shallowClone()
    {
        return shallowCloneImpl(new PrefixQuery());
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
