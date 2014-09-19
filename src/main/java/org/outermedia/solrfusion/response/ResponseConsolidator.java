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
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A simple consolidator which neither supports paging or sorting. All hits of all search servers are returned in a
 * round-robin manner. highlights and facets are not supported.
 * <p/>
 * Created by ballmann on 04.06.14.
 */
@ToString
@Slf4j
public class ResponseConsolidator extends AbstractResponseConsolidator
{
    private List<ClosableIterator<Document, SearchServerResponseInfo>> responseStreams;
    private Configuration configuration;

    public void initConsolidator(Configuration config)
    {
        this.configuration = config;
    }

    /**
     * Factory creates instances only.
     */
    protected ResponseConsolidator()
    {
        super();
        responseStreams = new ArrayList<>();
    }

    @Override
    public synchronized void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting,Document facetFields)
    {
        try
        {
            responseStreams.add(getNewMappingClosableIterator(searchServerConfig, docIterator, ResponseTarget.DOCUMENT));
        }
        catch (Exception e)
        {
            log.error("Caught exception while adding document responses of server {}",
                searchServerConfig.getSearchServerName(), e);
        }
    }

    protected MappingClosableIterator getNewMappingClosableIterator(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, ResponseTarget target)
        throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, configuration, searchServerConfig, null, target);
    }

    public int numberOfResponseStreams()
    {
        return responseStreams.size();
    }

    public void clear()
    {
        for (ClosableIterator<Document, SearchServerResponseInfo> docIterator : responseStreams)
        {
            docIterator.close();
        }
        responseStreams.clear();
        errorResponses.clear();
    }

    @Override
    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(FusionRequest fusionRequest)
    {
        return new DefaultClosableIterator(responseStreams);
    }

    @Override public List<Document> completelyMapMergedDoc(Collection<Document> sameDocuments,
        Map<String, Document> highlighting) throws InvocationTargetException, IllegalAccessException
    {
        // not supported
        return null;
    }

    public static class Factory
    {
        public static ResponseConsolidatorIfc getInstance()
        {
            return new ResponseConsolidator();
        }
    }

    @Override
    public void init(ResponseConsolidatorFactory config)
    {
        // NOP
    }


}
