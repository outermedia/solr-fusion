package org.outermedia.solrfusion.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 7/9/14.
 */
public abstract class AbstractResponseConsolidator implements ResponseConsolidatorIfc
{
    protected List<Exception> errorResponses;

    public AbstractResponseConsolidator()
    {
        errorResponses = new ArrayList<>();
    }

    @Override public void addErrorResponse(Exception se)
    {
        errorResponses.add(se);
    }

    public String getErrorMsg()
    {
        StringBuilder sb = new StringBuilder();
        if(errorResponses != null)
        {
            for(Exception ex : errorResponses)
            {
                sb.append(ex.getMessage());
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
