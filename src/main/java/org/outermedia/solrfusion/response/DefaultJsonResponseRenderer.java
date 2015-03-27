package org.outermedia.solrfusion.response;

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

import lombok.ToString;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a Solr search result into json.
 *
 * @author stephan
 */

@ToString
public class DefaultJsonResponseRenderer implements TextResponseRendererIfc
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
    public void writeResponse(Configuration configuration,
        ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request,
        FusionResponse fusionResponse)
    {
        freemarkerResponseRenderer.writeResponse(configuration, docStream, request, fusionResponse);
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
