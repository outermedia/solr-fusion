package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.response.parser.WordCount;

import java.util.*;

/**
 * Created by ballmann on 8/14/14.
 */
public class FacetWordCountSorter
{
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
                List<WordCount> sortableWordCounts = new ArrayList<>();
                for (Map.Entry<String, Integer> wordEntry : wordCounts.entrySet())
                {
                    WordCount wc = new WordCount();
                    wc.setWord(wordEntry.getKey());
                    wc.setCount(wordEntry.getValue());
                    wc.setSortByCount(FusionRequest.SORT_COUNT.equals(sortingOfFacetField));
                    sortableWordCounts.add(wc);
                }
                sortWordCounts(sortableWordCounts, sortingOfFacetField);
                result.put(fusionField, sortableWordCounts);
            }
        }
        return result;
    }

    /**
     * @param sortableWordCounts  is either {@link org.outermedia.solrfusion.FusionRequest#SORT_INDEX} or {@link
     *                            org.outermedia.solrfusion.FusionRequest#SORT_COUNT}
     * @param sortingOfFacetField
     */
    protected void sortWordCounts(List<WordCount> sortableWordCounts, String sortingOfFacetField)
    {
        Collections.sort(sortableWordCounts);
        if(sortingOfFacetField.equals(FusionRequest.SORT_COUNT))
        {
            Collections.reverse(sortableWordCounts);
        }
    }
}
