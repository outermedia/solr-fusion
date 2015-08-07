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
import java.util.*;
import java.util.regex.Pattern;

/**
 * Map a SolrFusion query to a dismax solr query.
 * <p/>
 *
 * @author stephan / sballmann
 */
@Slf4j
public class DisMaxToMySqlQueryBuilder extends EdismaxToMySqlQueryBuilder
{

    @Override
    public void init(QueryBuilderFactory config) throws InvocationTargetException, IllegalAccessException
    {
    }

    /**
     * Factory creates instances only.
     */
    private DisMaxToMySqlQueryBuilder()
    {
    }

    public static class Factory
    {
        public static QueryBuilderIfc getInstance()
        {
            return new DisMaxToMySqlQueryBuilder();
        }
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

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

    @Override
    protected StringBuilder handleBoolClauses(List<BooleanClause> clauses)
    {
        StringBuilder boolQueryStringBuilder = new StringBuilder();
        List<BooleanClause> requiredClauses = new ArrayList<>();
        List<BooleanClause> optionalClauses = new ArrayList<>();
        for (BooleanClause booleanClause : clauses)
        {
            if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST ||
                    booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
            {
                requiredClauses.add(booleanClause);
            } else
            {
                optionalClauses.add(booleanClause);
            }
        }
        String requiredSql = mapClauses(requiredClauses, true);
        String optionalSql = mapClauses(optionalClauses, false);
        boolean hasRequiredClauses = requiredSql.length() > 0;
        boolean hasOptionalClauses = optionalSql.length() > 0;
        boolean hasRequiredAndOptionalClauses = hasRequiredClauses && hasOptionalClauses;

        if (hasRequiredAndOptionalClauses)
        {
            boolQueryStringBuilder.append("(");
        }
        if (hasRequiredClauses) boolQueryStringBuilder.append(requiredSql);
        if (hasRequiredAndOptionalClauses) boolQueryStringBuilder.append(" AND ");
        if (hasOptionalClauses) boolQueryStringBuilder.append(optionalSql);
        if (hasRequiredAndOptionalClauses)
        {
            boolQueryStringBuilder.append(")");
        }

        return boolQueryStringBuilder;
    }

    /**
     * @param clauses
     * @param useAnd  use "AND" if true else "OR"
     * @return
     */
    private String mapClauses(List<BooleanClause> clauses, boolean useAnd)
    {
        String op = useAnd ? " AND " : " OR ";
        StringBuilder sql = new StringBuilder();
        for (BooleanClause booleanClause : clauses)
        {
            QueryBuilderIfc newClauseQueryBuilder = newQueryBuilder();
            String clauseQueryStr = newClauseQueryBuilder
                    .buildQueryStringWithoutNew(booleanClause.getQuery(), configuration, searchServerConfig, locale,
                            defaultSearchServerSearchFields, target, null);
            if (clauseQueryStr.length() > 0)
            {
                if (sql.length() > 0)
                {
                    sql.append(op);
                }
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                {
                    sql.append("!(");
                }
                sql.append(clauseQueryStr);
                if (booleanClause.getOccur() == BooleanClause.Occur.OCCUR_MUST_NOT)
                {
                    sql.append(")");
                }
            }
        }
        String result = sql.toString();
        if (result.length() > 0)
        {
            if (!useAnd)
            {
                result += " OR true";
            }
            result = "(" + result + ")";
        }
        return result;
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

    protected QueryBuilderIfc newQueryBuilder()
    {
        return new DisMaxToMySqlQueryBuilder();
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
