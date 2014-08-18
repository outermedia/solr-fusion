package org.outermedia.solrfusion.response;

import lombok.ToString;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a search result into php.
 *
 * @author ballmann
 */

@ToString
public class DefaultPhpResponseRenderer implements ResponseRendererIfc
{

    /**
     * Factory creates instances only.
     */
    protected DefaultPhpResponseRenderer()
    {
    }

    @Override
    public String getResponseString(Configuration configuration,
        ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request,
        FusionResponse fusionResponse)
    {
        return null; // TODO
    }

    public static class Factory
    {
        public static DefaultPhpResponseRenderer getInstance()
        {
            return new DefaultPhpResponseRenderer();
        }
    }

    @Override
    public void init(ResponseRendererFactory config)
    {
        // TODO Auto-generated method stub

    }
}
