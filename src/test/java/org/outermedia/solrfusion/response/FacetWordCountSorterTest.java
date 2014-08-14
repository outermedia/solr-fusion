package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.response.parser.WordCount;

import java.util.*;

/**
 * Created by ballmann on 8/14/14.
 */
public class FacetWordCountSorterTest
{
    @Test
    public void testDefaultSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 1, "c", 3);
        FusionRequest req = new FusionRequest();
        // no limit or other facet sort params given, sort by index
        Map<String, List<WordCount>> sortedFacets = new FacetWordCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<WordCount>> expected = buildSortedFacets("a", 1, "b", 2, "c", 3);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testDefaultCountSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        // limit > 0 enables sorting by count
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        Map<String, List<WordCount>> sortedFacets = new FacetWordCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<WordCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testCountSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        // global sorting overwrites defaul sorting
        req.setFacetSort(new SolrFusionRequestParam("count"));
        Map<String, List<WordCount>> sortedFacets = new FacetWordCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<WordCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testIndexSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 1, "c", 3);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        // global sorting overwrites defaul sorting
        req.setFacetSort(new SolrFusionRequestParam("index"));
        Map<String, List<WordCount>> sortedFacets = new FacetWordCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<WordCount>> expected = buildSortedFacets("a", 1, "b", 2, "c", 3);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testFieldSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        req.setFacetSort(new SolrFusionRequestParam("index"));
        // field sorting overwrites global index sorting
        req.setFacetSortFields(Arrays.asList(new SolrFusionRequestParam("count", "any")));
        Map<String, List<WordCount>> sortedFacets = new FacetWordCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<WordCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    protected Map<String, Map<String, Integer>> buildFacets(Object... args)
    {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        Map<String, Integer> facetsOfField = new HashMap<>();
        for (int i = 0; i < args.length; i += 2)
        {
            facetsOfField.put((String) args[i], (Integer) args[i + 1]);
        }
        result.put("any", facetsOfField);
        return result;
    }

    protected Map<String, List<WordCount>> buildSortedFacets(Object... args)
    {
        Map<String, List<WordCount>> result = new HashMap<>();
        List<WordCount> facetsOfField = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2)
        {
            WordCount wc = new WordCount();
            wc.setWord((String) args[i]);
            wc.setCount((Integer) args[i + 1]);
            facetsOfField.add(wc);
        }
        result.put("any", facetsOfField);
        return result;
    }
}
