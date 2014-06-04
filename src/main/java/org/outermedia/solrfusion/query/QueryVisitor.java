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
    public void visitQuery(MultiPhraseQuery t, ScriptEnv env);
    public void visitQuery(NumericRangeQuery t, ScriptEnv env);
    public void visitQuery(PhraseQuery t, ScriptEnv env);
    public void visitQuery(PrefixQuery t, ScriptEnv env);
    public void visitQuery(WildcardQuery t, ScriptEnv env);
}
