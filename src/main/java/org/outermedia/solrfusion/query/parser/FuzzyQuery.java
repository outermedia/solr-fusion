package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
public class FuzzyQuery extends TermQuery
{

    public FuzzyQuery(Term term, int maxEdits, int prefixLength)
    {
        super(term);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
