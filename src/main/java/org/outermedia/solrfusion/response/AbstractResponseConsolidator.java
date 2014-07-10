package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.adapter.SearchServerResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 7/9/14.
 */
public abstract class AbstractResponseConsolidator implements ResponseConsolidatorIfc
{
    protected List<SearchServerResponseException> errorResponses;

    public AbstractResponseConsolidator()
    {
        errorResponses = new ArrayList<>();
    }

    @Override public void addErrorResponse(SearchServerResponseException se)
    {
        errorResponses.add(se);
    }

    public String getErrorMsg()
    {
        StringBuilder sb = new StringBuilder();
        if(errorResponses != null)
        {
            for(SearchServerResponseException ex : errorResponses)
            {
                sb.append(ex.getMessage());
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
