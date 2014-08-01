package org.outermedia.solrfusion.response;

import lombok.ToString;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a search result into json.
 * 
 * @author stephan
 * 
 */

@ToString
public class DefaultJsonResponseRenderer implements ResponseRendererIfc
{
    private FreemarkerResponseRenderer freemarkerResponseRenderer;

    /**
     * Factory creates instances only.
     */
    protected DefaultJsonResponseRenderer()
    {
        freemarkerResponseRenderer = new FreemarkerResponseRenderer();
    }

    @Override
    public String getResponseString(Configuration configuration, ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request,
        FusionResponse fusionResponse)
    {
        return freemarkerResponseRenderer.getResponseString(configuration, docStream, request, fusionResponse);
    }

    public static class Factory
    {
        public static DefaultJsonResponseRenderer getInstance()
        {
            return new DefaultJsonResponseRenderer();
        }
    }

    @Override
    public void init(ResponseRendererFactory config)
    {
        freemarkerResponseRenderer.init(config);
        freemarkerResponseRenderer.setTemplateFile(freemarkerResponseRenderer.JSONTEMPLATEFILE);
    }

}
