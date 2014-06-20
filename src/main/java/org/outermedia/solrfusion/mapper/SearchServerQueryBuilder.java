package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.SearchServerQueryBuilderFactory;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map a fusion query to a solr request.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class SearchServerQueryBuilder implements SearchServerQueryBuilderIfc
{
    protected List<Query> newQueries;
    protected StringBuilder queryBuilder;

    /**
     * Build the query string for a search server.
     *
     * @param query the query to map to process
     */
    @Override
    public String buildQueryString(Query query)
    {
        newQueries = new ArrayList<>();
        queryBuilder = new StringBuilder();
        query.accept(this, null);
        return queryBuilder.toString();
    }

    @Override
    public void init(SearchServerQueryBuilderFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    /**
     * Factory creates instances only.
     */
    private SearchServerQueryBuilder()
    {
    }

    public static class Factory
    {
        public static SearchServerQueryBuilderIfc getInstance()
        {
            return new SearchServerQueryBuilder();
        }
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
        List<Query> l = term.getNewQueryTerms();
        if (l != null)
        {
            newQueries.addAll(l);
        }
    }

    protected void buildSearchServerTermQuery(Term term, boolean quoted)
    {
        if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            queryBuilder.append(term.getSearchServerFieldName());
            queryBuilder.append(":");
            if (quoted)
            {
                queryBuilder.append('"');
            }
            queryBuilder.append(term.getSearchServerFieldValue().get(0));
            if (quoted)
            {
                queryBuilder.append('"');
            }
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
