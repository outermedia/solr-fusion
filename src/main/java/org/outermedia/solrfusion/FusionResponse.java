package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.configuration.Message;
import org.outermedia.solrfusion.configuration.ResponseRendererType;

/**
 * Created by ballmann on 04.06.14.
 */
@Getter
@Setter
public class FusionResponse
{
    private String responseBody;

    private String errorMessage;

    private boolean ok;

    public FusionResponse()
    {
        ok = false;
        errorMessage = "Unknown";
    }

    protected void setOk()
    {
        ok = true;
        errorMessage = null;
    }

    protected void setError(String message)
    {
        ok = false;
        this.errorMessage = message;
    }

    /**
     * Parsing of the fusion query failed.
     * @param query
     */
    public void setResponseForQueryParseError(String query)
    {
        setError("Query parsing failed: " + query);
    }

    /**
     * At least one search server has to be configured in the fusion schema.
     */
    public void setResponseForNoSearchServerConfiguredError()
    {
        setError("No search server configured at all.");
    }

    /**
     * For too few search servers a response was received.
     *
     * @param disasterMessage
     */
    public void setResponseForTooLessServerAnsweredError(Message disasterMessage)
    {
        setError(disasterMessage.getText());
    }

    /**
     * Transform the found documents into a string using the requested format (xml, json, php). Call {@link #isOk()}
     * before calling this method here.
     *
     * @return a non null String instance if response is OK
     */
    public String getResponseAsString()
    {
        if (!ok)
        {
            return null;
        }
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
        setError("Found no configuration for response renderer: " + type);
    }

    public void setOkResponse(String responseBody)
    {
        setOk();
        this.responseBody = responseBody;
    }

    public boolean requestSucceeded()
    {
        return ok;
    }

    public void setResponseForException(Throwable lastException)
    {
        String reason = "unknown";
        if(lastException != null)
        {
            reason = lastException.getMessage();
        }
        setError("Internal processing error. Reason: " + reason);
    }
}
