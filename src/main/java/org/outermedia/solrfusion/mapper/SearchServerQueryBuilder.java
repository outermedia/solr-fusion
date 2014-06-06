package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Map a fusion query to a solr request.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class SearchServerQueryBuilder implements QueryVisitor
{
    protected List<Query> newQueries;
    protected StringBuilder queryBuilder;

    /**
     * Build the query string for a search server.
     *
     * @param query the query to map to process
     */
    public String buildQueryString(Query query)
    {
        newQueries = new ArrayList<>();
        queryBuilder = new StringBuilder();
        query.accept(this, null);
        return queryBuilder.toString();
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        t.visitTerm(this, env);
    }

    @Override
    public void visitQuery(Term term, ScriptEnv env)
    {
        buildSearchServerTermQuery(term, false);
        List<Query> l = term.getNewTerms();
        if (l != null)
        {
            newQueries.addAll(l);
        }
    }

    protected void buildSearchServerTermQuery(Term term, boolean quoted)
    {
        queryBuilder.append(term.getSearchServerFieldName());
        queryBuilder.append(":");
        if (quoted)
        {
            queryBuilder.append('"');
        }
        queryBuilder.append(term.getSearchServerFieldValue());
        if (quoted)
        {
            queryBuilder.append('"');
        }
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
        queryBuilder.append("*:*");
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

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        queryBuilder.append(" ");
        switch (booleanClause.getOccur())
        {
            case OCCUR_MUST:
                queryBuilder.append("+");
                break;
            case OCCUR_MUST_NOT:
                queryBuilder.append("-");
                break;
            default:
                // NOP
        }
        booleanClause.accept(this, env);
    }

}
