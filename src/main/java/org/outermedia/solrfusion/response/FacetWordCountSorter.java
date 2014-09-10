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
import org.outermedia.solrfusion.response.parser.WordCount;

import java.util.*;

/**
 * Because facets are merged from different Solr servers, SolrFusion has to sort the facets manually.
 *
 * Created by ballmann on 8/14/14.
 */
public class FacetWordCountSorter
{
    /**
     *  Sort facets by index or count.
     *
     * @param fusionFacetFields the key maps a field to a map of words and their word counts.
     * @param fusionRequest
     * @return
     */
    public Map<String, List<WordCount>> sort(Map<String, Map<String, Integer>> fusionFacetFields,
        FusionRequest fusionRequest)
    {
        Map<String, List<WordCount>> result = null;
        if (fusionFacetFields != null)
        {
            result = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : fusionFacetFields.entrySet())
            {
                String fusionField = entry.getKey();
                String sortingOfFacetField = fusionRequest.getSortingOfFacetField(fusionField);
                Map<String, Integer> wordCounts = entry.getValue();
                List<WordCount> sortedWordCounts = new ArrayList<>();
                for (Map.Entry<String, Integer> wordEntry : wordCounts.entrySet())
                {
                    WordCount wc = new WordCount();
                    wc.setWord(wordEntry.getKey());
                    wc.setCount(wordEntry.getValue());
                    wc.setSortByCount(FusionRequest.SORT_COUNT.equals(sortingOfFacetField));
                    sortedWordCounts.add(wc);
                }
                sortWordCounts(sortedWordCounts, sortingOfFacetField);
                filterWordCounts(sortedWordCounts, fusionRequest.getLimitOfFacetField(fusionField));
                result.put(fusionField, sortedWordCounts);
            }
        }
        return result;
    }

    /**
     * Remove too many word count entries. {@code limitOfFacetField} controls the length.
     *
     * @param sortedWordCounts
     * @param limitOfFacetField see {@link org.outermedia.solrfusion.FusionRequest#getLimitOfFacetField(String)}
     */
    protected void filterWordCounts(List<WordCount> sortedWordCounts, int limitOfFacetField)
    {
        if (limitOfFacetField >= 0)
        {
            int at = sortedWordCounts.size() - 1;
            while (at >= 0 && (at + 1) > limitOfFacetField)
            {
                sortedWordCounts.remove(at);
                at--;
            }
        }
    }

    /**
     * @param sortableWordCounts  is either {@link org.outermedia.solrfusion.FusionRequest#SORT_INDEX} or {@link
     *                            org.outermedia.solrfusion.FusionRequest#SORT_COUNT}
     * @param sortingOfFacetField
     */
    protected void sortWordCounts(List<WordCount> sortableWordCounts, String sortingOfFacetField)
    {
        Collections.sort(sortableWordCounts);
        if (sortingOfFacetField.equals(FusionRequest.SORT_COUNT))
        {
            Collections.reverse(sortableWordCounts);
        }
    }
}
