package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by ballmann on 8/8/14.
 */
@Getter
@Setter
@ToString
@Slf4j
public class SolrFusionRequestParam
{
    private String value;
    private String paramNameVariablePart;
    private boolean containedInRequest;

    public SolrFusionRequestParam()
    {
        this(null, null);
    }

    public SolrFusionRequestParam(String value)
    {
        this(value, null);
    }

    public SolrFusionRequestParam(String value, String defaultValue)
    {
        this(value, null, defaultValue);
    }

    public SolrFusionRequestParam(String value, String paramNameVariablePart, String defaultValue)
    {
        if (value != null)
        {
            value = value.trim();
            this.containedInRequest = true;
        }
        else
        {
            if (defaultValue != null)
            {
                value = defaultValue;
                this.containedInRequest = false;
            }
        }
        this.value = value;
        this.paramNameVariablePart = paramNameVariablePart;
    }

    public int getValueAsInt(int defaultValue)
    {
        int result = defaultValue;
        if (value != null)
        {
            try
            {
                result = Integer.parseInt(value);
            }
            catch (Exception e)
            {
                log.error("Invalid int number. Can't parse int from '{}'.", value, e);
            }
        }
        return result;
    }
}
