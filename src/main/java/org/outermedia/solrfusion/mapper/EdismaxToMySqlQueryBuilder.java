package org.outermedia.solrfusion.mapper;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Map a SolrFusion query to a sql query.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
@Slf4j
public class EdismaxToMySqlQueryBuilder extends AbstractQueryBuilder
{
    protected StringBuilder queryBuilder;
    protected Configuration configuration;
    protected SearchServerConfig searchServerConfig;
    protected Locale locale;
    protected Set<String> defaultSearchServerSearchFields;
    protected QueryTarget target;
    protected String operator;

    @Getter
    protected Pattern escapePattern = Pattern.compile("(['\\\\])", Pattern.CASE_INSENSITIVE);

    /**
     * Build the query string for a search server.
     *
     * @param query              the query to process
     * @param configuration
     * @param searchServerConfig
     * @param locale
     * @param target
     */
    @Override
    public String buildQueryString(Query query, Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale, Set<String> defaultSearchServerSearchFields, QueryTarget target)
    {
        String result = buildQueryStringWithoutNew(query, configuration, searchServerConfig, locale,
            defaultSearchServerSearchFields, target, null);

        return result;
    }

    // inside add queried have been processed, now add the outside queries
    public String getStaticallyAddedQueries(Configuration configuration, SearchServerConfig searchServerConfig,
        Locale locale, QueryTarget target, String result)
    {
        List<String> newQueriesToAdd = new ArrayList<>();
        AddOperation addOp = new AddOperation();
        Map<String, List<Target>> allAddQueryTargets = searchServerConfig.findAllAddQueryMappings(AddLevel.OUTSIDE,
            target);
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
        SearchServerConfig searchServerConfig, Locale locale, Set<String> defaultSearchServerSearchFields,
        QueryTarget target, Object unused)
    {
        queryBuilder = new StringBuilder();
        this.configuration = configuration;
        this.searchServerConfig = searchServerConfig;
        this.locale = locale;
        this.defaultSearchServerSearchFields = defaultSearchServerSearchFields;
        this.target = target;
        ScriptEnv env = new ScriptEnv();
        env.setBinding("op", (operator != null)?operator:"like");
        query.accept(this, env);
        return queryBuilder.toString();
    }

    @Override
    public void escapeSearchWord(StringBuilder queryBuilder, boolean quoted, String searchWord,
                                 DefaultFieldType fusionFieldType)
    {
        Pattern p;
        if (searchWord.contains("'") || searchWord.contains("\\") || DefaultFieldType.DATE == fusionFieldType ||
                DefaultFieldType.TEXT == fusionFieldType)
        {
            quoted = true;
        }
        if (quoted)
        {
            queryBuilder.append("'");
            p = escapePhrasePattern;
        } else
        {
            p = getEscapePattern();
        }
        if(DefaultFieldType.DATE == fusionFieldType)
        {
            // e.g. 20140626
            searchWord =
                    searchWord.substring(0, 4) + "-" + searchWord.substring(4, 6) + "-" + searchWord.substring(6, 8) +
                            " 00:00:00";
        }
        String s = escape(p, searchWord);
        queryBuilder.append(s);
        if (quoted)
        {
            queryBuilder.append("'");
        }
    }

    @Override
    public String escape(Pattern p, String s)
    {
        return p.matcher(s).replaceAll("\\\\$1");
    }

    @Override
    public void init(QueryBuilderFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    /**
     * Factory creates instances only.
     */
    protected EdismaxToMySqlQueryBuilder()
    {
    }

    public static class Factory
    {
        public static QueryBuilderIfc getInstance()
        {
            return new EdismaxToMySqlQueryBuilder();
        }
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        buildSearchServerTermQuery(t.getTerm(), false, t.getBoostValue(), t, env.getStringBinding("op"));
    }

    protected boolean buildSearchServerTermQuery(Term term, boolean quoted, Float boost, Query origQuery, String operator)
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
                    searchServerConfig, locale, defaultSearchServerSearchFields, target, null);
                insideClauses.add(clauseQueryStr);
            }
            else
            {
                // can't preserve {!...} for all newly added queries
                // handleMetaInfo(origQuery.getMetaInfo(), queryBuilder);
            }
            added = handleNewQueries(newQueries, insideClauses);
            // restore original value
            term.setNewQueries(newQueries);
        }
        else if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            // can't map meta info
            // handleMetaInfo(origQuery.getMetaInfo(), queryBuilder);
            added = true;
            String mysqlFieldName = term.getSearchServerFieldName();
            if(operator.equals("like"))
            {
                queryBuilder.append(buildMysqlFieldName(mysqlFieldName));
                queryBuilder.append(" like ");
                String searchWord = "%"+term.getSearchServerFieldValue().get(0)+"%";
                escapeSearchWord(queryBuilder, true, searchWord, term.getFusionField().getFieldType());
            }
            else if(operator.equals("prefix") || operator.equals("wildcard"))
            {
                queryBuilder.append(buildMysqlFieldName(mysqlFieldName));
                queryBuilder.append(" like ");
                String searchWord = term.getSearchServerFieldValue().get(0).replace("*","%");
                escapeSearchWord(queryBuilder, true, searchWord, term.getFusionField().getFieldType());
            }
            else if(operator.equals("sounds like"))
            {
                queryBuilder.append(buildMysqlFieldName(mysqlFieldName));
                queryBuilder.append(" sounds like ");
                String searchWord = term.getSearchServerFieldValue().get(0);
                escapeSearchWord(queryBuilder, true, searchWord, term.getFusionField().getFieldType());
            }
            else
            {
                queryBuilder.append(buildMysqlFieldName(mysqlFieldName));
                queryBuilder.append(operator);
                String searchWord = term.getSearchServerFieldValue().get(0);
                escapeSearchWord(queryBuilder, quoted, searchWord, term.getFusionField().getFieldType());
            }
            // sql adapter has to build boost handleBoost(boost);
        }

        return added;
    }

    protected String buildMysqlFieldName(String mysqlFieldName)
    {
        return "`"+ mysqlFieldName.replace(".","`.`") +"`";
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
            // in SOLR 'or' means maybe, so allow to lack ( OR true)
            queryBuilder.append(" OR true)");
        }
        return added;
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
        boolean lastWasOr = false;
        for (BooleanClause booleanClause : clauses)
        {
            QueryBuilderIfc newClauseQueryBuilder = newQueryBuilder();
            String clauseQueryStr = newClauseQueryBuilder.buildQueryStringWithoutNew(booleanClause.getQuery(),
                configuration, searchServerConfig, locale, defaultSearchServerSearchFields, target, null);
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
                lastWasOr = BooleanClause.Occur.OCCUR_SHOULD == booleanClause.getOccur();
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                {
                    boolQueryStringBuilder.append("!(");
                }
                boolQueryStringBuilder.append(clauseQueryStr);
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                {
                    boolQueryStringBuilder.append(")");
                }
            }
        }
        if(lastWasOr)
        {
            // in solr 'or' means maybe, so no hit is OK too
            boolQueryStringBuilder.append(" OR true");
        }

        return boolQueryStringBuilder;
    }

    protected QueryBuilderIfc newQueryBuilder()
    {
        return new EdismaxToMySqlQueryBuilder();
    }

    @Override
    public void visitQuery(FuzzyQuery fq, ScriptEnv env)
    {
        // TODO use levenshtein instead of "sounds like"
        buildSearchServerTermQuery(fq.getTerm(), false, fq.getBoostValue(), fq, "sounds like");
    }


    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        queryBuilder.append("1=1");
    }

    @Override
    public void visitQuery(PhraseQuery pq, ScriptEnv env)
    {
        buildSearchServerTermQuery(pq.getTerm(), true, pq.getBoostValue(), pq, "like");
    }

    @Override
    public void visitQuery(PrefixQuery pq, ScriptEnv env)
    {
        buildSearchServerTermQuery(pq.getTerm(), false, pq.getBoostValue(), pq, "prefix");
    }

    @Override
    public void visitQuery(WildcardQuery wq, ScriptEnv env)
    {
        buildSearchServerTermQuery(wq.getTerm(), false, wq.getBoostValue(), wq, "wildcard");
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        // NOP, not used
    }

    @Override
    public void visitQuery(SubQuery t, ScriptEnv env)
    {
        queryBuilder.append("(");
        Query subQuery = t.getQuery();
        QueryBuilderIfc newQueryBuilder = null;
        if (subQuery.isDismaxQuery())
        {
            try
            {
                newQueryBuilder = searchServerConfig.getDismaxQueryBuilder(configuration.getDismaxQueryBuilder());
            }
            catch (Exception e)
            {
                log.error("Caught exception while creating new dismax query builder instance", e);
            }
        }
        else
        {
            try
            {
                newQueryBuilder = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            }
            catch (Exception e)
            {
                log.error("Caught exception while creating new query builder.", e);
            }
        }

        String subQueryStr = newQueryBuilder.buildQueryString(subQuery, configuration, searchServerConfig, locale,
            defaultSearchServerSearchFields, target);
        subQueryStr = newQueryBuilder.getStaticallyAddedQueries(configuration, searchServerConfig, locale, target,
            subQueryStr);

        queryBuilder.append(subQueryStr);
        queryBuilder.append(")");
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
                    searchServerConfig, locale, defaultSearchServerSearchFields, target, null);
                insideClauses.add(clauseQueryStr);
            }
            handleNewQueries(newQueries, insideClauses);
        }
        else if (term.isWasMapped() && !term.isRemoved() && term.getSearchServerFieldValue() != null)
        {
            // min and max apply to the same field
            String mysqlFieldName = buildMysqlFieldName(rq.getSearchServerFieldName());
            boolean hasLeftValue = !"*".equals(rq.getMinSearchServerValue());
            boolean hasRightValue = !"*".equals(rq.getMaxSearchServerValue());
            boolean hasMinMaxValue = hasLeftValue && hasRightValue;
            if(hasMinMaxValue)
            {
                queryBuilder.append("(");
            }
            if(hasLeftValue)
            {
                queryBuilder.append(mysqlFieldName);
                queryBuilder.append(" >= ");
                escapeSearchWord(queryBuilder,false,rq.getMinSearchServerValue(),term.getFusionField().getFieldType());
            }
            if(hasMinMaxValue)
            {
                queryBuilder.append(" AND ");
            }
            if(hasRightValue)
            {
                queryBuilder.append(mysqlFieldName);
                queryBuilder.append(" <= ");
                escapeSearchWord(queryBuilder, false, rq.getMaxSearchServerValue(),
                        term.getFusionField().getFieldType());
            }
            if(hasMinMaxValue)
            {
                queryBuilder.append(")");
            }
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
