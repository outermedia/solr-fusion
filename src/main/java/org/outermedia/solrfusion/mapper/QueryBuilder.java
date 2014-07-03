package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryBuilderFactory;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Map a fusion query to a solr request which is parsable by an edismax parser.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class QueryBuilder implements QueryBuilderIfc
{
    protected List<Query> newQueries;
    protected StringBuilder queryBuilder;
    protected Configuration configuration;

    /**
     * Build the query string for a search server.
     *
     * @param query         the query to map to process
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

    public StringBuilder getQueryBuilderOutput()
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
    private QueryBuilder()
    {
    }

    public static class Factory
    {
        public static QueryBuilderIfc getInstance()
        {
            return new QueryBuilder();
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
        List<Query> l = term.getNewQueryTerms();
        if (l != null)
        {
            newQueries.addAll(l);
        }
        return added;
    }

    protected boolean buildSearchServerTermQuery(Term term, boolean quoted, Float boost)
    {
        boolean added = false;
        if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            added = true;
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
            handleBoost(boost);
        }
        return added;
    }

    private void handleBoost(Float boost)
    {
        if (boost != null)
        {
            queryBuilder.append("^");
            queryBuilder.append(boost);
        }
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        List<BooleanClause> clauses = t.getClauses();
        if (clauses != null)
        {
            StringBuilder boolQueryStringBuilder = new StringBuilder();
            for (BooleanClause booleanClause : clauses)
            {
                QueryBuilderIfc newClauseQueryBuilder = newQueryBuilder();
                newClauseQueryBuilder.buildQueryString(booleanClause.getQuery(), configuration);
                StringBuilder clauseQueryStr = newClauseQueryBuilder.getQueryBuilderOutput();
                if(clauseQueryStr.length() > 0)
                {
                    if (boolQueryStringBuilder.length() > 0)
                    {
                        switch (booleanClause.getOccur())
                        {
                            // -X (must not) makes no sense in combination with OR
                            case OCCUR_MUST_NOT:
                            case OCCUR_MUST:
                                boolQueryStringBuilder.append(" AND ");
                                break;
                            case OCCUR_SHOULD:
                                boolQueryStringBuilder.append(" OR ");
                                break;
                        }
                    }
                    // "+" is redundant for AND, but in the case that all previous clauses were deleted, it
                    // is not possible to decide whether to print out a "+" or not
                    if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST)
                    {
                        boolQueryStringBuilder.append("+");
                    }
                    if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                    {
                        boolQueryStringBuilder.append("-");
                    }
                    boolQueryStringBuilder.append(clauseQueryStr);
                }
            }
            if(boolQueryStringBuilder.length() > 0)
            {
                queryBuilder.append("(");
                queryBuilder.append(boolQueryStringBuilder);
                queryBuilder.append(")");
            }
        }
    }

    protected QueryBuilderIfc newQueryBuilder()
    {
        return new QueryBuilder();
    }

    @Override
    public void visitQuery(FuzzyQuery fq, ScriptEnv env)
    {
        if (buildSearchServerTermQuery(fq.getTerm(), false, fq.getBoostValue()))
        {
            handleFuzzySlop(fq.getMaxEdits(), true);
        }
    }

    protected void handleFuzzySlop(Integer maxEdits, boolean force)
    {
        if (maxEdits != null || force)
        {
            // TODO solr3 uses 0..1, solr4=0..2, OK to map without value?
            queryBuilder.append("~");
        }
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        queryBuilder.append("*:*");
    }

    @Override
    public void visitQuery(PhraseQuery pq, ScriptEnv env)
    {
        if (buildSearchServerTermQuery(pq.getTerm(), true, pq.getBoostValue()))
        {
            handleFuzzySlop(pq.getMaxEdits(), false);
        }
    }

    @Override
    public void visitQuery(PrefixQuery pq, ScriptEnv env)
    {
        buildSearchServerTermQuery(pq.getTerm(), false, pq.getBoostValue());
    }

    @Override
    public void visitQuery(WildcardQuery wq, ScriptEnv env)
    {
        buildSearchServerTermQuery(wq.getTerm(), false, wq.getBoostValue());
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        // NOP, not used
    }

    protected void buildSearchServerRangeQuery(NumericRangeQuery<?> rq)
    {
        Term min = rq.getMin();
        Term max = rq.getMax();
        // min and max apply to the same field
        queryBuilder.append(min.getSearchServerFieldName());
        queryBuilder.append(":");
        queryBuilder.append("[");
        queryBuilder.append(min.getSearchServerFieldValue().get(0));
        queryBuilder.append(" TO ");
        queryBuilder.append(max.getSearchServerFieldValue().get(0));
        queryBuilder.append("]");
        handleBoost(rq.getBoostValue());
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        buildSearchServerRangeQuery(t);
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        buildSearchServerRangeQuery(t);
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        buildSearchServerRangeQuery(t);
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        buildSearchServerRangeQuery(t);
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        buildSearchServerRangeQuery(t);
    }
}
