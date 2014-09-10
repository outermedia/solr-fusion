package org.outermedia.solrfusion.query.parser;

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
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.VisitableQuery;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * A boolean clause is one query part of a boolean query.
 */
@Getter
@Setter
@ToString
public class BooleanClause implements VisitableQuery
{
    public enum Occur
    {
        OCCUR_MUST, OCCUR_SHOULD, OCCUR_MUST_NOT
    }

    private Occur occur;
    private Query query;


    public BooleanClause(Query query, Occur occur)
    {
        this.query = query;
        this.occur = occur;
    }

    public boolean isProhibited()
    {
        return occur == Occur.OCCUR_MUST_NOT;
    }

    public boolean isRequired()
    {
        return Occur.OCCUR_MUST == occur;
    }

    public boolean isOptional()
    {
        return Occur.OCCUR_SHOULD == occur;
    }


    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        query.accept(visitor, env);
    }

}
