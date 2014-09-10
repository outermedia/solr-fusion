package org.outermedia.solrfusion;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Message;
import org.outermedia.solrfusion.configuration.ResponseRendererType;

import java.util.List;

/**
 * Data holder to store SolrFusions response.
 *
 * Created by ballmann on 04.06.14.
 */
@Getter
@Setter
@Slf4j
public class FusionResponse
{
    private String responseBody;

    private String errorMessage;

    private boolean ok;

    private long qStart;
    private long qEnd;

    public FusionResponse()
    {
        ok = false;
        errorMessage = "Unknown";
    }

    public void setOk()
    {
        ok = true;
        errorMessage = null;
        qEnd = System.currentTimeMillis();
    }

    protected void setError(String message, String cause)
    {
        log.error("Error while processing query: {}", message);
        ok = false;
        this.errorMessage = message;
        if (cause != null && cause.length() > 0)
        {
            this.errorMessage += "\nCause: " + cause;
        }
        qEnd = System.currentTimeMillis();
    }

    /**
     * Parsing of the fusion query failed.
     *
     * @param queryList
     * @param cause
     */
    public void setResponseForQueryParseError(List<String> queryList, String cause)
    {
        StringBuilder sb = new StringBuilder();
        for (String qs : queryList)
        {
            sb.append(qs);
            sb.append(";");
        }
        setError("Query parsing failed: " + sb.toString(), cause);
    }

    /**
     * At least one search server has to be configured in the fusion schema.
     */
    public void setResponseForNoSearchServerConfiguredError()
    {
        setError("No search server configured at all.", null);
    }

    /**
     * For too few search servers a response was received.
     *
     * @param disasterMessage
     * @param errorMsg
     */
    public void setResponseForTooLessServerAnsweredError(Message disasterMessage, String errorMsg)
    {
        setError(disasterMessage.getText(), errorMsg);
    }

    /**
     * Transform the found documents into a string using the requested format (xml, json, php). Call {@link #isOk()}
     * before calling this method here.
     *
     * @return a non null String instance if response is OK
     */
    public String getResponseAsString()
    {
        return responseBody;
    }

    /**
     * No response renderer was configured in the fusion schema for the required renderer type.
     *
     * @param requestedType is the requested, but unknown type
     */
    public void setResponseForMissingResponseRendererError(ResponseRendererType requestedType)
    {
        String type = "<unknown>";
        if (requestedType != null)
        {
            type = requestedType.toString();
        }
        setError("Found no configuration for response renderer: " + type, null);
    }

    public void setOkResponse(String responseBody)
    {
        setOk();
        this.responseBody = responseBody;
    }

    public void setErrorResponse(String responseBody)
    {
        this.responseBody = responseBody;
    }

    public boolean requestSucceeded()
    {
        return ok;
    }

    public void setResponseForException(Throwable lastException)
    {
        String reason = "unknown";
        if (lastException != null)
        {
            reason = lastException.getMessage();
        }
        setError("Internal processing error. Reason: " + reason, null);
    }

    public void setResponseForException(List<Throwable> exceptions)
    {
        String reason = "unknown";
        if (exceptions != null)
        {
            StringBuilder sb = new StringBuilder();
            for (Throwable t : exceptions)
            {
                if (sb.length() > 0)
                {
                    sb.append("; ");
                }
                sb.append(t.getMessage());
            }
            reason = sb.toString();
        }
        setError("Internal processing error. Reason: " + reason, null);
    }

    public long getQueryTime()
    {
        return qEnd - qStart;
    }
}
