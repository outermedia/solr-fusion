package org.outermedia.solrfusion.adapter.solr;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.utils.URIBuilder;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;

import java.net.URISyntaxException;

/**
 * Created by ballmann on 9/17/14.
 */
@Getter
@Setter
public class SolrFusionUriBuilder extends URIBuilder implements SolrFusionUriBuilderIfc
{
    private boolean builtForDismax;

    public SolrFusionUriBuilder(String url) throws URISyntaxException
    {
        super(url);
    }
}
