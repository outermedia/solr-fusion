package org.outermedia.solrfusion.query;

import lombok.Getter;

/**
 * Created by ballmann on 7/4/14.
 */
@Getter
public enum SolrFusionRequestParams
{
    QUERY("q"), WRITER_TYPE("wt"), FILTER_QUERY("fq"), START("start"), PAGE_SIZE("rows"), SORT(
    "sort"), FIELDS_TO_RETURN("fl"), HIGHLIGHT_FIELDS_TO_RETURN("hl.fl"), HIGHLIGHT_PRE(
    "hl.simple.pre"), HIGHLIGHT_POST("hl.simple.post"), HIGHLIGHT_QUERY("hl.q"), HIGHLIGHT("hl");

    protected String requestParamName;

    SolrFusionRequestParams(String n)
    {
        requestParamName = n;
    }
}
