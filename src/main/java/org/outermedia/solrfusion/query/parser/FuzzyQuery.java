package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
@Getter
public class FuzzyQuery extends TermQuery
{

    private int maxEdits;

    public FuzzyQuery(Term term, int maxEdits)
    {
        super(term);
        this.maxEdits = maxEdits;
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
