package org.outermedia.solrfusion.response;

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

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.response.parser.DocCount;

import java.util.*;

/**
 * Because facets are merged from different Solr servers, SolrFusion has to sort the facets manually.
 *
 * Created by ballmann on 8/14/14.
 */
public class FacetDocCountSorter
{
    /**
     *  Sort facets by index or count.
     *
     * @param fusionFacetFields the key maps a field to a map of words and their doc counts.
     * @param fusionRequest
     * @return
     */
    public Map<String, List<DocCount>> sort(Map<String, Map<String, Integer>> fusionFacetFields,
        FusionRequest fusionRequest)
    {
        Map<String, List<DocCount>> result = null;
        if (fusionFacetFields != null)
        {
            result = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : fusionFacetFields.entrySet())
            {
                String fusionField = entry.getKey();
                String sortingOfFacetField = fusionRequest.getSortingOfFacetField(fusionField);
                Map<String, Integer> docCounts = entry.getValue();
                List<DocCount> sortedDocCounts = new ArrayList<>();
                for (Map.Entry<String, Integer> wordEntry : docCounts.entrySet())
                {
                    DocCount wc = new DocCount();
                    wc.setWord(wordEntry.getKey());
                    wc.setCount(wordEntry.getValue());
                    wc.setSortByCount(FusionRequest.SORT_COUNT.equals(sortingOfFacetField));
                    sortedDocCounts.add(wc);
                }
                sortDocCounts(sortedDocCounts, sortingOfFacetField);
                filterDocCounts(sortedDocCounts, fusionRequest.getLimitOfFacetField(fusionField));
                result.put(fusionField, sortedDocCounts);
            }
        }
        return result;
    }

    /**
     * Remove too many doc count entries. {@code limitOfFacetField} controls the length.
     *
     * @param sortedDocCounts
     * @param limitOfFacetField see {@link org.outermedia.solrfusion.FusionRequest#getLimitOfFacetField(String)}
     */
    protected void filterDocCounts(List<DocCount> sortedDocCounts, int limitOfFacetField)
    {
        if (limitOfFacetField >= 0)
        {
            int at = sortedDocCounts.size() - 1;
            while (at >= 0 && (at + 1) > limitOfFacetField)
            {
                sortedDocCounts.remove(at);
                at--;
            }
        }
    }

    /**
     * @param sortableDocCounts  is either {@link org.outermedia.solrfusion.FusionRequest#SORT_INDEX} or {@link
     *                            org.outermedia.solrfusion.FusionRequest#SORT_COUNT}
     * @param sortingOfFacetField
     */
    protected void sortDocCounts(List<DocCount> sortableDocCounts, String sortingOfFacetField)
    {
        Collections.sort(sortableDocCounts);
        if (sortingOfFacetField.equals(FusionRequest.SORT_COUNT))
        {
            Collections.reverse(sortableDocCounts);
        }
    }
}
