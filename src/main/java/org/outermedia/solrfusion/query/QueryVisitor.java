package org.outermedia.solrfusion.query;

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

import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Visitor pattern for queries.
 *
 * Created by ballmann on 03.06.14.
 */
public interface QueryVisitor
{
    public void visitQuery(TermQuery t, ScriptEnv env);
    public void visitQuery(BooleanQuery t, ScriptEnv env);
    public void visitQuery(FuzzyQuery t, ScriptEnv env);
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env);
    public void visitQuery(IntRangeQuery t, ScriptEnv env);
    public void visitQuery(LongRangeQuery t, ScriptEnv env);
    public void visitQuery(FloatRangeQuery t, ScriptEnv env);
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env);
    public void visitQuery(DateRangeQuery t, ScriptEnv env);
    public void visitQuery(PhraseQuery t, ScriptEnv env);
    public void visitQuery(PrefixQuery t, ScriptEnv env);
    public void visitQuery(WildcardQuery t, ScriptEnv env);
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env);
    public void visitQuery(SubQuery t, ScriptEnv env);
}
