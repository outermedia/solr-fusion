package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Map a fusion query to a solr request.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class ResetQueryState implements QueryVisitor
{

    public void reset(Query query)
    {
        query.accept(this, null);
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm(), env, null);
    }

    protected boolean visitQuery(Term t, ScriptEnv env, Float boost)
    {
        t.resetQuery();
        return true;
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm(), env, null);
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // NOP
    }

    protected void resetNumericRangeQuery(NumericRangeQuery<?> rq, ScriptEnv env)
    {
        visitQuery(rq.getMin(), env, null);
        visitQuery(rq.getMax(), env, null);
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        resetNumericRangeQuery(t, env);
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        resetNumericRangeQuery(t, env);
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        resetNumericRangeQuery(t, env);
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        resetNumericRangeQuery(t, env);
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        resetNumericRangeQuery(t, env);
    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm(), env, null);
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm(), env, null);
    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm(), env, null);
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        booleanClause.accept(this, env);
    }

}
