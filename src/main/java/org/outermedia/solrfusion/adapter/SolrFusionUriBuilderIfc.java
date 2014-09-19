package org.outermedia.solrfusion.adapter;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Created by ballmann on 9/18/14.
 */
public interface SolrFusionUriBuilderIfc
{
    public boolean isBuiltForDismax();

    public void setBuiltForDismax(boolean flag);

    public List<NameValuePair> getQueryParams();
}
