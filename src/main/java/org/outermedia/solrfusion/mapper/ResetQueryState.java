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
        t.getTerm().resetQuery();
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        visitQuery((TermQuery) t, env);
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // TODO expand * to all fields in order to apply add/remove operations?!
    }

    @Override
    public void visitQuery(MultiPhraseQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(NumericRangeQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        // TODO

    }

}
