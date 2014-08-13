package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by ballmann on 8/8/14.
 */
@Getter
@Setter
@ToString
public class SolrFusionRequestParam
{
    private String value;
    private String paramNameVariablePart;

    public SolrFusionRequestParam(String value)
    {
        this(value, null);
    }

    public SolrFusionRequestParam(String value, String paramNameVariablePart)
    {
        if (value != null)
        {
            value = value.trim();
        }
        this.value = value;
        this.paramNameVariablePart = paramNameVariablePart;
    }
}
