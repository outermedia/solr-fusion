package org.outermedia.solrfusion.adapter.solr;

import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;

/**
 * Created by ballmann on 8/1/14.
 */
public class Solr1Adapter extends DefaultSolrAdapter
{
    /**
     * Factory creates instances only.
     */
    protected Solr1Adapter()
    {
        QUERY_PARAMETER = "q.alt";
    }

    public static class Factory
    {
        public static SearchServerAdapterIfc getInstance()
        {
            return new Solr1Adapter();
        }
    }
}
