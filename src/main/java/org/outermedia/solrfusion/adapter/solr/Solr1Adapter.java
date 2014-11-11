package org.outermedia.solrfusion.adapter.solr;

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

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This special Solr adapter respects that Solr version less than 1.4 expect true/false instead of index/count for facet
 * sorting.
 * <p/>
 * Created by ballmann on 8/1/14.
 */
@Slf4j
public class Solr1Adapter extends DefaultSolrAdapter
{
    /**
     * Factory creates instances only.
     */
    protected Solr1Adapter()
    {
    }

    @Override public InputStream sendQuery(SolrFusionUriBuilder uriBuilder, int timeout) throws URISyntaxException, IOException
    {

        return super.sendQuery(uriBuilder, timeout);
    }

    @Override protected void buildFacetHttpClientParams(Multimap<String> params, URIBuilder ub)
    {
        // Solr Doc: Prior to Solr1.4, one needed to use true instead of count and false instead of index.
        if (solrVersion != null && solrVersion < 1.4)
        {
            String facetSort = params.getFirst(FACET_SORT);
            if (facetSort != null)
            {
                params.set(FACET_SORT, getOldSortValue(facetSort));
            }
            List<Map.Entry<String, String>> sortFields = params.filterBy(FACET_SORT_FIELD);
            for (Map.Entry<String, String> sfEntry : sortFields)
            {
                params.set(sfEntry.getKey(), getOldSortValue(sfEntry.getValue()));
            }
        }
        super.buildFacetHttpClientParams(params, ub);
    }

    private String getOldSortValue(String facetSort)
    {
        if (facetSort.equals(FusionRequest.SORT_COUNT))
        {
            facetSort = "true";
        }
        else
        {
            facetSort = "false";
        }
        return facetSort;
    }

    public static class Factory
    {
        public static SearchServerAdapterIfc getInstance()
        {
            return new Solr1Adapter();
        }
    }
}
