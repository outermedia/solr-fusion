package org.outermedia.solrfusion.mapper;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Map a fusion query to a solr request which is parsable by an edismax parser.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
@Slf4j
public class QueryBuilder implements QueryBuilderIfc
{
    protected StringBuilder queryBuilder;
    protected Configuration configuration;
    protected SearchServerConfig searchServerConfig;
    protected Locale locale;

    /**
     * Build the query string for a search server.
     *
     * @param query              the query to process
     * @param configuration
     * @param searchServerConfig
     * @param locale
     */
    @Override
    public String buildQueryString(Query query, Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale)
    {
        String result = buildQueryStringWithoutNew(query, configuration, searchServerConfig, locale);

        // inside add queried have been processed, now add the outside queries
        List<String> newQueriesToAdd = new ArrayList<>();
        AddOperation addOp = new AddOperation();
        Map<String, List<Target>> allAddQueryTargets = searchServerConfig.findAllAddQueryMappings(AddLevel.OUTSIDE);
        for (Map.Entry<String, List<Target>> entry : allAddQueryTargets.entrySet())
        {
            String searchServerFieldName = entry.getKey();
            List<Target> addQuery = entry.getValue();
            for (Target t : addQuery)
            {
                newQueriesToAdd.addAll(addOp.addToQuery(configuration, searchServerFieldName, t, locale));
            }
        }
        if (newQueriesToAdd.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            if (result.length() > 0)
            {
                sb.append('(');
                sb.append(result);
                sb.append(')');
            }
            for (String qs : newQueriesToAdd)
            {
                if (sb.length() > 0)
                {
                    sb.append(" AND ");
                }
                sb.append(qs);
            }
            result = sb.toString();
        }

        return result;
    }

    @Override
    public String buildQueryStringWithoutNew(Query query, Configuration configuration,
        SearchServerConfig searchServerConfig, Locale locale)
    {
        queryBuilder = new StringBuilder();
        this.configuration = configuration;
        this.searchServerConfig = searchServerConfig;
        this.locale = locale;
        query.accept(this, null);
        return queryBuilder.toString();
    }

    @Override
    public void init(QueryBuilderFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    /**
     * Factory creates instances only.
     */
    protected QueryBuilder()
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
        buildSearchServerTermQuery(t.getTerm(), false, t.getBoostValue(), t);
    }

    protected boolean buildSearchServerTermQuery(Term term, boolean quoted, Float boost, Query origQuery)
    {
        boolean added = false;
        List<String> newQueries = term.getNewQueries();
        // avoid endless recursion
        term.setNewQueries(null);
        if (term.isWasMapped() && newQueries != null)
        {
            List<String> insideClauses = new ArrayList<>();
            if (!term.isRemoved())
            {
                String clauseQueryStr = newQueryBuilder().buildQueryStringWithoutNew(origQuery, configuration,
                    searchServerConfig, locale);
                insideClauses.add(clauseQueryStr);
            }
            added = handleNewQueries(newQueries, insideClauses);
        }
        else if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
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

    private boolean handleNewQueries(List<String> newQueries, List<String> insideClauses)
    {
        boolean added = false;
        for (String qs : newQueries)
        {
            // inside only
            insideClauses.add(qs);
        }
        if (insideClauses.size() > 0)
        {
            added = true;
            queryBuilder.append("(");
            // queryBuilder.append(handleBoolClauses(insideClauses));
            for (int i = 0; i < insideClauses.size(); i++)
            {
                String qs = insideClauses.get(i);
                if (i > 0)
                {
                    queryBuilder.append(" OR ");
                }
                queryBuilder.append(qs);
            }
            queryBuilder.append(")");
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
            StringBuilder boolQueryStringBuilder = handleBoolClauses(clauses);
            if (boolQueryStringBuilder.length() > 0)
            {
                queryBuilder.append("(");
                queryBuilder.append(boolQueryStringBuilder);
                queryBuilder.append(")");
            }
        }
    }

    protected StringBuilder handleBoolClauses(List<BooleanClause> clauses)
    {
        StringBuilder boolQueryStringBuilder = new StringBuilder();
        for (BooleanClause booleanClause : clauses)
        {
            QueryBuilderIfc newClauseQueryBuilder = newQueryBuilder();
            String clauseQueryStr = newClauseQueryBuilder.buildQueryStringWithoutNew(booleanClause.getQuery(),
                configuration, searchServerConfig, locale);
            if (clauseQueryStr.length() > 0)
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

        return boolQueryStringBuilder;
    }

    protected QueryBuilderIfc newQueryBuilder()
    {
        return new QueryBuilder();
    }

    @Override
    public void visitQuery(FuzzyQuery fq, ScriptEnv env)
    {
        if (buildSearchServerTermQuery(fq.getTerm(), false, fq.getBoostValue(), fq))
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
        if (buildSearchServerTermQuery(pq.getTerm(), true, pq.getBoostValue(), pq))
        {
            handleFuzzySlop(pq.getMaxEdits(), false);
        }
    }

    @Override
    public void visitQuery(PrefixQuery pq, ScriptEnv env)
    {
        buildSearchServerTermQuery(pq.getTerm(), false, pq.getBoostValue(), pq);
    }

    @Override
    public void visitQuery(WildcardQuery wq, ScriptEnv env)
    {
        buildSearchServerTermQuery(wq.getTerm(), false, wq.getBoostValue(), wq);
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        // NOP, not used
    }

    protected void buildSearchServerRangeQuery(NumericRangeQuery<?> rq)
    {
        Term term = rq.getTerm();
        List<String> newQueries = term.getNewQueries();
        // avoid endless recursion
        term.setNewQueries(null);
        if (term.isWasMapped() && newQueries != null)
        {
            List<String> insideClauses = new ArrayList<>();
            if (!term.isRemoved())
            {
                String clauseQueryStr = newQueryBuilder().buildQueryStringWithoutNew(rq, configuration,
                    searchServerConfig, locale);
                insideClauses.add(clauseQueryStr);
            }
            handleNewQueries(newQueries, insideClauses);
        }
        else if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            // min and max apply to the same field
            queryBuilder.append(rq.getSearchServerFieldName());
            queryBuilder.append(":");
            queryBuilder.append("[");
            queryBuilder.append(rq.getMinSearchServerValue());
            queryBuilder.append(" TO ");
            queryBuilder.append(rq.getMaxSearchServerValue());
            queryBuilder.append("]");
            handleBoost(rq.getBoostValue());
        }
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
