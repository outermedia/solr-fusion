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

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Data holder which stores the values of one Solr field and the corresponding SolrFusion field.
 *
 * @author sballmann
 */

@ToString(exclude = {"fusionFacetCount", "searchServerFacetCount"})
@Getter
@Setter
@Slf4j
public class Term
{
    private String fusionFieldName;
    private List<String> fusionFieldValue;
    private FusionField fusionField;
    private List<Integer> fusionFacetCount;

    private String searchServerFieldName;
    private List<String> searchServerFieldValue;
    private List<Integer> searchServerFacetCount;

    // set by an remove operation
    private boolean removed;

    // set by a change operation
    private boolean wasMapped;

    private boolean processed;

    // added by an add operation
    private List<String> newQueries;


    private Term()
    {
        removed = false;
        wasMapped = false;
        processed = false;
    }

    public static Term newFusionTerm(String field, String... termStr)
    {
        return newFusionTerm(field, Arrays.asList(termStr));
    }

    public static Term newFusionTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.fusionFieldName = field;
        result.fusionFieldValue = new ArrayList<>();
        if (termStr != null)
        {
            result.fusionFieldValue.addAll(termStr);
        }
        return result;
    }

    public static Term newSearchServerTerm(String field, String... termStr)
    {
        return newSearchServerTerm(field, Arrays.asList(termStr));
    }

    public static Term newSearchServerTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.searchServerFieldName = field;
        result.searchServerFieldValue = new ArrayList<>();
        result.searchServerFieldValue.addAll(termStr);
        return result;
    }

    public String field()
    {
        return fusionFieldName;
    }

    public void resetQuery()
    {
        searchServerFieldName = null;
        searchServerFieldValue = null;
        searchServerFacetCount = null;
        removed = false;
        wasMapped = false;
        processed = false;
        newQueries = null;
    }

    public void resetSearchServerField()
    {
        fusionFieldName = null;
        fusionFieldValue = null;
        fusionFacetCount = null;
        fusionField = null;
        removed = false;
        wasMapped = false;
        processed = false;
    }

    public int compareFusionValue(Term t)
    {
        // sort the uncomparable to one end
        int unknownReturnValue = -1;
        if (t == null)
        {
            return unknownReturnValue;
        }
        List<String> thisFusionValues = getFusionFieldValue();
        List<String> otherFusionValues = t.getFusionFieldValue();
        if (thisFusionValues == null && otherFusionValues == null)
        {
            return 0;
        }
        if (thisFusionValues == null)
        {
            return -unknownReturnValue;
        }
        if (otherFusionValues == null)
        {
            return unknownReturnValue;
        }
        int thisSize = thisFusionValues.size();
        int otherSize = otherFusionValues.size();
        if (thisSize >= 1)
        {
            if (otherSize == 0)
            {
                return unknownReturnValue;
            }
            else
            {
                // for multi values only the first field is used!
                String otherValue = otherFusionValues.get(0);
                String thisValue = thisFusionValues.get(0);

                // do a number compare and not a string compare for "score"
                if (ResponseMapperIfc.FUSION_FIELD_NAME_SCORE.equals(fusionFieldName))
                {
                    return Double.valueOf(thisValue).compareTo(Double.valueOf(otherValue));
                }

                return thisValue.compareTo(otherValue);
            }
        }
        else
        {
            if (otherSize == 0)
            {
                return 0;
            }
            return -unknownReturnValue;
        }
    }

    /**
     * Add a new query part to a search server's query.
     *
     * @param inside                 if true or outside (false)
     * @param searchServerFieldValue one complete search server query
     * @param configuration
     * @param locale
     * @param addField true if the generating ScriptType doesn't "returnsFullQueries" (then a simple word)
     */
    public void addNewSearchServerQuery(boolean inside, List<String> searchServerFieldValue,
        Configuration configuration, Locale locale, boolean addField)
    {
        if (newQueries == null)
        {
            newQueries = new ArrayList<>();
        }
        // qb.init() not called! but sufficient for escaping
        // The used edismax query builder below escapes more chars than the dismax builder, but a dismax parser
        // should understand the additional escaped chars too
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        // TODO what if searchServerFieldValue.size() == 0?!
        for (String qs : searchServerFieldValue)
        {
            // qs contains whole query, otherwise it would be difficult to mix-in the field name, because complex
            // queries are possible here
            StringBuilder queryBuilder = new StringBuilder();
            if (inside && searchServerFieldName != null && addField)
            {
                queryBuilder.append(searchServerFieldName);
                queryBuilder.append(":");
                qb.escapeSearchWord(queryBuilder, false, qs);
            }
            else
            {
                queryBuilder.append(qs);
            }
            newQueries.add(queryBuilder.toString());
        }
        setWasMapped(true);
    }

    public String mergeFusionValues()
    {
        String result = null;
        if (fusionFieldValue != null)
        {
            result = Joiner.on(",").join(fusionFieldValue);
        }
        return result;
    }

    public String mergeSearchServerValues()
    {
        String result = null;
        if (searchServerFieldValue != null)
        {
            result = Joiner.on(",").join(searchServerFieldValue);
        }
        return result;
    }

    public Term shallowClone()
    {
        Term newTerm = new Term();
        newTerm.fusionFieldName = fusionFieldName;
        newTerm.fusionFieldValue = fusionFieldValue;
        newTerm.fusionField = fusionField;
        newTerm.fusionFacetCount = fusionFacetCount;
        newTerm.searchServerFieldName = searchServerFieldName;
        newTerm.searchServerFieldValue = searchServerFieldValue;
        newTerm.searchServerFacetCount = searchServerFacetCount;
        newTerm.removed = removed;
        newTerm.wasMapped = wasMapped;
        newTerm.processed = processed;
        newTerm.newQueries = null;
        return newTerm;
    }

}
