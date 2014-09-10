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
 * Transforms a Solr search result into php. Not implemented.
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
