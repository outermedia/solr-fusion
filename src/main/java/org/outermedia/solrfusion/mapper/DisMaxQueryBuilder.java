package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryBuilderFactory;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map a fusion query to a solr request.
 * <p/>
 * @author stephan
 */
public class DisMaxQueryBuilder implements QueryBuilderIfc
{
    protected List<Query> newQueries;
    protected StringBuilder queryBuilder;
    protected Configuration configuration;

    /**
     * Build the query string for a search server.
     *
     * @param query the query to map to process
     * @param configuration
     */
    @Override
    public String buildQueryString(Query query, Configuration configuration)
    {
        newQueries = new ArrayList<>();
        queryBuilder = new StringBuilder();
        this.configuration = configuration;
        query.accept(this, null);
        return queryBuilder.toString();
    }

    @Override public StringBuilder getQueryBuilderOutput()
    {
        return queryBuilder;
    }

    @Override
    public void init(QueryBuilderFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    /**
     * Factory creates instances only.
     */
    private DisMaxQueryBuilder()
    {
    }

    public static class Factory
    {
        public static QueryBuilderIfc getInstance()
        {
            return new DisMaxQueryBuilder();
        }
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        t.visitTerm(this, env);
    }

    @Override
    public boolean visitQuery(Term term, ScriptEnv env, Float boost)
    {
        boolean added = buildSearchServerTermQuery(term, false, boost);
        return added;
    }

    protected boolean buildSearchServerTermQuery(Term term, boolean quoted, Float boost)
    {
        boolean added = false;
        if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            added = true;
            if (quoted)
            {
                queryBuilder.append('"');
            }
            queryBuilder.append(term.getSearchServerFieldValue().get(0));
            if (quoted)
            {
                queryBuilder.append('"');
            }
            if(boost != null)
            {
                queryBuilder.append("^");
                queryBuilder.append(boost);
            }
        }
        return added;
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        // TODO
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax parser
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // NOP, dismax doesn't know *:*, so leave empty
    }

    @Override
    public void visitQuery(PhraseQuery pq, ScriptEnv env)
    {
        buildSearchServerTermQuery(pq.getTerm(), true, pq.getBoostValue());
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        // NOP, leave empty
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        // TODO
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

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        // TODO
    }
}
