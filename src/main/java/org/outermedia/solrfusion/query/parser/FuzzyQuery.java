package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * A fuzzy query.
 */
@ToString(callSuper = true)
@Getter
public class FuzzyQuery extends TermQuery
{

    private Integer maxEdits;

    public FuzzyQuery(Term term, Integer maxEdits)
    {
        super(term);
        this.maxEdits = maxEdits;
    }

    protected FuzzyQuery() {}

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    @Override
    public FuzzyQuery shallowClone()
    {
        FuzzyQuery fq = shallowCloneImpl(new FuzzyQuery());
        fq.maxEdits = maxEdits;
        return fq;
    }
}
