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
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

/**
 * This class represents a simple Solr term query aka "&lt;field&gt;:&lt;value&gt;".
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Getter
@Setter
public class TermQuery extends Query
{
    private Term term;

    public TermQuery(Term term)
    {
        this.term = term;
    }

    protected TermQuery()
    {
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    public String getFusionFieldName()
    {
        return term.getFusionFieldName();
    }

    public List<String> getFusionFieldValue()
    {
        return term.getFusionFieldValue();
    }

    public String getFirstFusionFieldValue()
    {
        String result = null;
        List<String> values = term.getFusionFieldValue();
        if (values != null && values.size() > 0)
        {
            result = values.get(0);
        }
        return result;
    }

    public String getSearchServerFieldName()
    {
        return term.getSearchServerFieldName();
    }

    public void setSearchServerFieldName(String s)
    {
        term.setSearchServerFieldName(s);
    }

    public List<String> getSearchServerFieldValue()
    {
        return term.getSearchServerFieldValue();
    }

    protected <T extends TermQuery> T shallowCloneImpl(T result)
    {
        result.setTerm(getTerm().shallowClone());
        result.setBoostValue(getBoostValue());
        if (getMetaInfo() != null)
        {
            result.setMetaInfo(getMetaInfo().shallowClone());
        }
        return result;
    }

    public TermQuery shallowClone()
    {
        return shallowCloneImpl(new TermQuery());
    }

    public void resetQuery()
    {
        super.resetQuery();
        term.resetQuery();
    }

    public boolean isSearchServerFieldEmpty()
    {
        List<String> searchServerValues = getSearchServerFieldValue();
        return searchServerValues == null || searchServerValues.isEmpty() || term.isRemoved();
    }
}
