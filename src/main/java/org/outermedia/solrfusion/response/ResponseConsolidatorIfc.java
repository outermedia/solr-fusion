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

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementations of this class have to combine several Solr responses into one document stream. The returned documents
 * are valid regarding to the SolrFusion schema.
 * <p/>
 * Created by ballmann on 04.06.14.
 */
public interface ResponseConsolidatorIfc extends Initiable<ResponseConsolidatorFactory>
{
    /**
     * Initialize the consolidator.
     *
     * @param config the SolrFusion schema
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void initConsolidator(Configuration config) throws InvocationTargetException, IllegalAccessException;

    /**
     * Add another Solr response for processing.
     *
     * @param searchServerConfig the current destination Solr server configuration
     * @param docIterator        the Solr documents
     * @param request            the current SolrFusion request
     * @param highlighting       the highlight part of the current Solr response
     * @param facetFields        the facet part of the current Solr response
     */
    public void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting, Document facetFields);

    /**
     * Get the number of added Solr responses i.e. calls of {@link #addResultStream(org.outermedia.solrfusion.configuration.SearchServerConfig,
     * ClosableIterator, org.outermedia.solrfusion.FusionRequest, java.util.List, org.outermedia.solrfusion.response.parser.Document)}.
     *
     * @return
     */
    public int numberOfResponseStreams();

    /**
     * Free internally created data.
     */
    public void clear();

    /**
     * Get the processed documents ready for sending back to SolrFusion's caller.
     *
     * @param fusionRequest the current SolrFusion request
     * @return an perhaps empty ClosableIterator
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(FusionRequest fusionRequest)
        throws InvocationTargetException, IllegalAccessException;

    /**
     * Store the exception of a failed Solr communication.
     * @param se
     */
    public void addErrorResponse(Exception se);

    /**
     * Build a combined string from all calls of {@link #addErrorResponse(Exception)}.
     * @return an error message containing all collected errors
     */
    public String getErrorMsg();

    /**
     * Deprecated method, used only internally by PagingResponseConsolidator. Should be removed from this interface.
     */
    public List<Document> completelyMapMergedDoc(Collection<Document> sameDocuments, Map<String, Document> highlighting)
        throws InvocationTargetException, IllegalAccessException;

}
