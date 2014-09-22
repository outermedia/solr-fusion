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

import junit.framework.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.response.parser.DocCount;

import java.util.*;

/**
 * Created by ballmann on 8/14/14.
 */
public class FacetDocCountSorterTest
{
    @Test
    public void testDefaultSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 1, "c", 3);
        FusionRequest req = new FusionRequest();
        // no limit or other facet sort params given, sort by index
        Map<String, List<DocCount>> sortedFacets = new FacetDocCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<DocCount>> expected = buildSortedFacets("a", 1, "b", 2, "c", 3);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testDefaultCountSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        // limit > 0 enables sorting by count
        req.setFacetLimit(new SolrFusionRequestParam("20", null));
        Map<String, List<DocCount>> sortedFacets = new FacetDocCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<DocCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testCountSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20", null));
        // global sorting overwrites defaul sorting
        req.setFacetSort(new SolrFusionRequestParam("count", null));
        Map<String, List<DocCount>> sortedFacets = new FacetDocCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<DocCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testIndexSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 1, "c", 3);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20", null));
        // global sorting overwrites defaul sorting
        req.setFacetSort(new SolrFusionRequestParam("index", null));
        Map<String, List<DocCount>> sortedFacets = new FacetDocCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<DocCount>> expected = buildSortedFacets("a", 1, "b", 2, "c", 3);
        Assert.assertEquals("Expected other order", expected, sortedFacets);
    }

    @Test
    public void testFieldSorting()
    {
        Map<String, Map<String, Integer>> facets = buildFacets("b", 2, "a", 3, "c", 1);
        FusionRequest req = new FusionRequest();
        req.setFacetLimit(new SolrFusionRequestParam("20", null));
        req.setFacetSort(new SolrFusionRequestParam("index", null));
        // field sorting overwrites global index sorting
        req.setFacetSortFields(Arrays.asList(new SolrFusionRequestParam("count", "any", null)));
        Map<String, List<DocCount>> sortedFacets = new FacetDocCountSorter().sort(facets, req);
        // System.out.println("SF " + sortedFacets);
        Map<String, List<DocCount>> expected = buildSortedFacets("a", 3, "b", 2, "c", 1);
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

    protected Map<String, List<DocCount>> buildSortedFacets(Object... args)
    {
        Map<String, List<DocCount>> result = new HashMap<>();
        List<DocCount> facetsOfField = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2)
        {
            DocCount wc = new DocCount();
            wc.setWord((String) args[i]);
            wc.setCount((Integer) args[i + 1]);
            facetsOfField.add(wc);
        }
        result.put("any", facetsOfField);
        return result;
    }
}
