package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

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

    public void reset(List<Query> queryList)
    {
        for (Query query : queryList)
        {
            if (query != null)
            {
                query.accept(this, null);
            }
        }
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    protected boolean visitQuery(Term t)
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
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // NOP
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        visitQuery(t.getTerm());
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        booleanClause.accept(this, env);
    }

}
