package org.outermedia.solrfusion.mapper;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Map a fusion query to a solr request.
 * <p/>
 *
 * @author stephan / sballmann
 */
@Slf4j
public class DisMaxQueryBuilder implements QueryBuilderIfc
{
    protected List<Query> newQueries;
    protected StringBuilder queryBuilder;
    protected Configuration configuration;
    protected Locale locale;
    protected SearchServerConfig searchServerConfig;
    protected Set<String> defaultSearchServerSearchFields;

    /**
     * Build the query string for a search server.
     *
     * @param query              the query to map to process
     * @param configuration
     * @param searchServerConfig
     * @param locale
     */
    @Override
    public String buildQueryString(Query query, Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale, Set<String> defaultSearchServerSearchFields)
    {
        String result = buildQueryStringWithoutNew(query, configuration, searchServerConfig, locale,
            defaultSearchServerSearchFields);

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
                sb.append(result);
            }
            for (String qs : newQueriesToAdd)
            {
                sb.append(" ");
                sb.append(qs);
            }
            result = sb.toString();
        }


        return result;
    }

    @Override public String buildQueryStringWithoutNew(Query query, Configuration configuration,
        SearchServerConfig searchServerConfig, Locale locale, Set<String> defaultSearchServerSearchFields)
    {
        newQueries = new ArrayList<>();
        queryBuilder = new StringBuilder();
        this.searchServerConfig = searchServerConfig;
        this.configuration = configuration;
        this.defaultSearchServerSearchFields = defaultSearchServerSearchFields;
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
        buildSearchServerTermQuery(t.getTerm(), false, t.getBoostValue(), t);
    }

    protected boolean buildSearchServerTermQuery(Term term, boolean quoted, Float boost, Query origQuery)
    {
        boolean added = false;

        List<String> searchServerFieldValue = term.getSearchServerFieldValue();
        List<String> newQueries = term.getNewQueries();
        // avoid endless recursion
        term.setNewQueries(null);
        if (term.isWasMapped() && newQueries != null)
        {
            List<String> insideClauses = new ArrayList<>();
            if (!term.isRemoved())
            {
                String clauseQueryStr = newQueryBuilder().buildQueryStringWithoutNew(origQuery, configuration,
                    searchServerConfig, locale, defaultSearchServerSearchFields);
                insideClauses.add(clauseQueryStr);
            }
            else
            {
                // preserve {!...} for all newly added queries
                handleMetaInfo(origQuery.getMetaInfo(), queryBuilder);
            }
            added = handleNewQueries(newQueries, insideClauses);
            // restore original value
            term.setNewQueries(newQueries);
        }
        else if (term.isWasMapped() && !term.isRemoved() && searchServerFieldValue != null)
        {
            // TODO filter out duplicate search words?!
            if (defaultSearchServerSearchFields.contains(term.getSearchServerFieldName()))
            {
                handleMetaInfo(origQuery.getMetaInfo(), queryBuilder);
                added = true;
                if (quoted)
                {
                    queryBuilder.append('"');
                }
                String s = searchServerFieldValue.get(0);
                if (!quoted && isSpecialString(s))
                {
                    queryBuilder.append("\\");
                }
                queryBuilder.append(s);
                if (quoted)
                {
                    queryBuilder.append('"');
                }
                if (boost != null)
                {
                    queryBuilder.append("^");
                    queryBuilder.append(boost);
                }
            }
            else
            {
                log.debug("Can't add field, because it is not the default search field: {}:{} ",
                    term.getSearchServerFieldName(), searchServerFieldValue);
            }
        }
        return added;
    }

    protected boolean isSpecialString(String s)
    {
        // TODO more string which need escaping?
        return "-".equals(s);
    }

    protected boolean handleNewQueries(List<String> newQueries, List<String> insideClauses)
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
            for (int i = 0; i < insideClauses.size(); i++)
            {
                String qs = insideClauses.get(i);
                if (i > 0)
                {
                    queryBuilder.append(" ");
                }
                // no "+" means "or"
                queryBuilder.append(qs);
            }
        }
        return added;
    }

    protected void handleMetaInfo(MetaInfo metaInfo, StringBuilder builder)
    {
        // don't render {!dismax ...}
        if (metaInfo != null && !MetaInfo.DISMAX_PARSER.equals(metaInfo.getName()))
        {
            metaInfo.buildSearchServerQueryString(builder);
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
                queryBuilder.append(boolQueryStringBuilder);
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
                configuration, searchServerConfig, locale, defaultSearchServerSearchFields);
            if (clauseQueryStr.length() > 0)
            {
                if (boolQueryStringBuilder.length() > 0)
                {
                    boolQueryStringBuilder.append(" ");
                }
                // "+" is redundant for AND, but in the case that all previous clauses were deleted, it
                // is not possible to decide whether to print out a "+" or not
                boolean prependedOccurence = false;
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST)
                {
                    boolQueryStringBuilder.append("+");
                    prependedOccurence = true;
                }
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                {
                    boolQueryStringBuilder.append("-");
                    prependedOccurence = true;
                }
                if (prependedOccurence && clauseQueryStr.contains(" "))
                {
                    boolQueryStringBuilder.append("(");
                }
                boolQueryStringBuilder.append(clauseQueryStr);
                if (prependedOccurence && clauseQueryStr.contains(" "))
                {
                    boolQueryStringBuilder.append(")");
                }
            }
        }

        return boolQueryStringBuilder;
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
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        buildSearchServerTermQuery(t.getTerm(), true, t.getBoostValue(), t);
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        // NOP, leave empty

    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        // NOP, leave empty
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        // NOP, not used
    }

    @Override
    public void visitQuery(SubQuery t, ScriptEnv env)
    {
        t.getQuery().accept(this, env);
    }

    protected QueryBuilderIfc newQueryBuilder()
    {
        try
        {
            return configuration.getDismaxQueryBuilder();
        }
        catch (Exception e)
        {
            log.error("Caught exception while creating new dismax query builder instance", e);
            return null;
        }
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        // NOP, not supported by dismax
    }
}
