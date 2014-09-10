package org.outermedia.solrfusion.response.parser;

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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Base Class for SolrSingleValuedField and SolrMultiValuedField to be accessed uniformly.
 *
 * @author stephan
 */

@XmlTransient
@ToString(exclude = {"term"})
public abstract class SolrField
{

    @XmlAttribute(name = "name", required = true) @Getter @Setter
    protected String fieldName;

    @XmlTransient @Getter @Setter
    private Term term;

    public String getFirstSearchServerFieldValue()
    {
        String result = null;
        List<String> allValues = term.getSearchServerFieldValue();
        if (allValues != null && allValues.size() > 0)
        {
            result = allValues.get(0);
        }
        return result;
    }

    public List<String> getAllSearchServerFieldValue()
    {
        List<String> result = null;
        List<String> values = term.getSearchServerFieldValue();
        if (values != null)
        {
            result = new ArrayList<>(values);
        }
        return result;
    }

    public String getFirstFusionFieldValue()
    {
        String result = null;
        List<String> allValues = term.getFusionFieldValue();
        if (allValues != null && allValues.size() > 0)
        {
            result = allValues.get(0);
        }
        return result;

    }

    public List<String> getAllFusionFieldValue()
    {
        List<String> result = new ArrayList<>();
        List<String> values = term.getFusionFieldValue();
        if (values != null)
        {
            result.addAll(values);
        }
        return result;
    }

    public boolean isFusionField(String fusionFieldName)
    {
        return fusionFieldName.equals(term.getFusionFieldName());
    }

    public List<Integer> getFusionFacetCount()
    {
        return term.getFusionFacetCount();
    }

    public String getFusionFieldName()
    {
        return term.getFusionFieldName();
    }

    public boolean isProcessed() { return term.isProcessed(); }

    public boolean isRemoved() { return term.isRemoved(); }

    public List<Integer> getSearchServerFacetWordCounts()
    {
        return term.getSearchServerFacetCount();
    }
}
