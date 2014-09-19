package org.outermedia.solrfusion.adapter.solr;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.NameValuePair;
import org.apache.solr.client.solrj.SolrQuery;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;

import java.util.List;

/**
 * Created by ballmann on 9/18/14.
 */
@Getter
@Setter
public class SolrFusionSolrQuery extends SolrQuery implements SolrFusionUriBuilderIfc
{
    private boolean builtForDismax;

    public SolrFusionSolrQuery(String q)
    {
        super(q);
    }

    @Override public List<NameValuePair> getQueryParams()
    {
        throw new RuntimeException("Not implemented");
    }
}
