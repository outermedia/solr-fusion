package org.outermedia.solrfusion.query;

import lombok.Getter;

/**
 * Created by ballmann on 7/4/14.
 */
@Getter
public enum SolrFusionRequestParams
{
    QUERY("q"), WRITER_TYPE("wt"), FILTER_QUERY("fq"), START("start"), PAGE_SIZE("rows"), SORT("sort");

    protected String requestParamName;

    SolrFusionRequestParams(String n)
    {
        requestParamName = n;
    }
}
