package org.outermedia.solrfusion.adapter;

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

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Allows to send and receive data from a search server.
 *
 * @author ballmann
 */

public interface SearchServerAdapterIfc extends Initiable<SearchServerConfig>
{

    /**
     * Send the provided query to a search server and returns the retrieved documents as InputStream. In the case of an
     * error a {@link org.outermedia.solrfusion.adapter.SearchServerResponseException} is thrown.
     *
     * @param configuration         the SolrFusion schema
     * @param searchServerConfig    the current destination Solr server configuration
     * @param fusionRequest         the current SolrFusion request
     * @param searchServerQueryStr a query suitable for this search server.
     * @param timeout              a timeout in milliseconds
     * @param version              the version of the search server
     * @return null for error or a document stream
     */
    public InputStream sendQuery(Configuration configuration, SearchServerConfig searchServerConfig,
        FusionRequest fusionRequest, Multimap<String> searchServerQueryStr, int timeout, String version)
        throws URISyntaxException, IOException;

    public void init(SearchServerConfig config);

    public void setUrl(String url);

    public String getUrl();
}
