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
import org.outermedia.solrfusion.response.parser.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Allows to send and receive data from a search server.
 *
 * @author ballmann
 */

public interface SearchServerAdapterIfc<Url extends SolrFusionUriBuilderIfc> extends Initiable<SearchServerConfig>
{

    /**
     * Send the provided query to a search server and returns the retrieved documents as InputStream. In the case of an
     * error a {@link org.outermedia.solrfusion.adapter.SearchServerResponseException} is thrown.
     *
     * @param uriBuilder an object created by a previous call of {@link #buildHttpClientParams(org.outermedia.solrfusion.configuration.Configuration,
     *                   org.outermedia.solrfusion.configuration.SearchServerConfig, org.outermedia.solrfusion.FusionRequest,
     *                   org.outermedia.solrfusion.Multimap, String)}
     * @param timeout    a timeout in milliseconds
     * @return null for error or a document stream
     */
    public InputStream sendQuery(Url uriBuilder, int timeout) throws URISyntaxException, IOException;

    /**
     * Create an url suitable for a Solr server.
     *
     * @param params             the prepared Solr request parameters
     * @param fusionRequest      the current SolrFusion request
     * @param searchServerConfig the current destination Solr server configuration
     * @param configuration      the SolrFusion schema
     * @param version            the version of the search server
     * @return a new object
     * @throws URISyntaxException
     */
    public Url buildHttpClientParams(Configuration configuration, SearchServerConfig searchServerConfig,
        FusionRequest fusionRequest, Multimap<String> params, String version) throws URISyntaxException;

    public void init(SearchServerConfig config);

    public void setUrl(String url);

    public String getUrl();

    public void setSolrVersion(Double v);

    public Double getSolrVersion();

    public void finish() throws Exception;

    public void commitLastDocs();

    public void add(Document doc) throws Exception;

    public void deleteByQuery(String query) throws Exception;
}
