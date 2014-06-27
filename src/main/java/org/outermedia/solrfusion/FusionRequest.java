package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.configuration.ResponseRendererType;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 */
@Setter
@Getter
public class FusionRequest
{
    private String query;

    private Locale locale;

    private ResponseRendererType responseType;

    public FusionRequest()
    {
        responseType = ResponseRendererType.XML;
    }

    public Map<String, Float> getBoosts()
    {
        return new HashMap<>(); // TODO from request params
    }

    public void setResponseTypeFromString(String responseTypeStr) throws ServletException
    {
        if (responseTypeStr != null)
        {
            String trimmedResponseTypeStr = responseTypeStr.trim().toUpperCase();
            try
            {
                responseType = ResponseRendererType.valueOf(trimmedResponseTypeStr);
            }
            catch (Exception e)
            {
                throw new ServletException("Found no renderer for given type '" + trimmedResponseTypeStr + "'", e);
            }
        }
    }
}
