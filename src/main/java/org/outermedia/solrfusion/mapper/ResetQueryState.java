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

import org.outermedia.solrfusion.ParsedQuery;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

/**
 * Reset a query object, so that another mapping can be applied.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class ResetQueryState implements QueryVisitor
{

    public void reset(Query query)
    {
        query.accept(this, null);
    }

    public void reset(List<ParsedQuery> queryList)
    {
        for (ParsedQuery query : queryList)
        {
            if (query != null && query.getQuery() != null)
            {
                query.getQuery().accept(this, null);
            }
        }
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    protected boolean visitTermQuery(TermQuery tq)
    {
        tq.resetQuery();
        return true;
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // NOP
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        visitTermQuery(t);
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        booleanClause.accept(this, env);
    }

    @Override
    public void visitQuery(SubQuery t, ScriptEnv env)
    {
        t.getQuery().accept(this, env);
    }

}
