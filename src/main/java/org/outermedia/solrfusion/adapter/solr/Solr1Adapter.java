package org.outermedia.solrfusion.adapter.solr;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This special Solr adapter respects that Solr version less than 1.4 expect true/false instead of index/count
 * for facet sorting.
 *
 * Created by ballmann on 8/1/14.
 */
@Slf4j
public class Solr1Adapter extends DefaultSolrAdapter
{
    protected Double solrVersion;

    /**
     * Factory creates instances only.
     */
    protected Solr1Adapter()
    {
    }

    @Override public InputStream sendQuery(Configuration configuration, SearchServerConfig searchServerConfig,
        FusionRequest fusionRequest, Multimap<String> params, int timeout, String version)
        throws URISyntaxException, IOException
    {
        this.solrVersion = parseDouble(version);
        return super.sendQuery(configuration, searchServerConfig, fusionRequest, params, timeout, version);
    }

    protected Double parseDouble(String version)
    {
        Double result = null;
        try
        {
            result = Double.valueOf(version);
        }
        catch (Exception e)
        {
            log.error("Can't parse search server version '{}'", version, e);
        }

        return result;
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
