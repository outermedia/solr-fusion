package org.outermedia.solrfusion.query;

import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Visitor pattern.
 *
 * Created by ballmann on 03.06.14.
 */
public interface QueryVisitor
{
    public void visitQuery(TermQuery t, ScriptEnv env);
    public void visitQuery(BooleanQuery t, ScriptEnv env);
    public void visitQuery(FuzzyQuery t, ScriptEnv env);
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env);
    public void visitQuery(IntRangeQuery t, ScriptEnv env);
    public void visitQuery(LongRangeQuery t, ScriptEnv env);
    public void visitQuery(FloatRangeQuery t, ScriptEnv env);
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env);
    public void visitQuery(DateRangeQuery t, ScriptEnv env);
    public void visitQuery(PhraseQuery t, ScriptEnv env);
    public void visitQuery(PrefixQuery t, ScriptEnv env);
    public void visitQuery(WildcardQuery t, ScriptEnv env);
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env);
    public void visitQuery(SubQuery t, ScriptEnv env);
}
