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

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * A ClosableIteratorIfc which maps a Solr document to the SolrFusion schema before the document is returned.
 *
 * Created by ballmann on 6/11/14.
 */
@Slf4j
public class MappingClosableIterator implements ClosableIterator<Document, SearchServerResponseInfo>
{
    protected Set<String> fieldsToMap;
    protected Configuration config;
    protected SearchServerConfig searchServerConfig;
    protected ClosableIterator<Document, SearchServerResponseInfo> documents;

    protected boolean ignoreUnmappedFields = true;
    protected Document nextDoc;
    protected ResponseTarget target;

    /**
     * @param documents
     * @param config
     * @param searchServerConfig
     * @param fieldsToMap        either null (for all) or a list of field names to process
     * @param target
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public MappingClosableIterator(ClosableIterator<Document, SearchServerResponseInfo> documents, Configuration config,
        SearchServerConfig searchServerConfig, Set<String> fieldsToMap, ResponseTarget target)
        throws InvocationTargetException, IllegalAccessException
    {
        this.config = config;
        this.documents = documents;
        this.searchServerConfig = searchServerConfig;
        this.fieldsToMap = fieldsToMap;
        this.target = target;
    }

    @Override
    public boolean hasNext()
    {
        while (documents.hasNext())
        {
            nextDoc = documents.next();
            ScriptEnv env = getNewScriptEnv();
            try
            {
                int mappedFieldNr = config.getResponseMapper().mapResponse(config, searchServerConfig, nextDoc, env,
                    fieldsToMap, target);
                // no field was mapped = no fusion value present -> ignore the empty document
                // because the id is always mapped, this should not happen
                if (mappedFieldNr == 0)
                {
                    log.debug("Ignoring unmapped doc from server {}: {}", searchServerConfig.getSearchServerName(),
                        nextDoc);
                    continue;
                }
                if (nextDoc != null)
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                log.error("Mapping failed. Document is ignored. See previous errors in log file for reasons.", e);
            }
        }
        nextDoc = null;
        return false;
    }

    protected ScriptEnv getNewScriptEnv()
    {
        return new ScriptEnv();
    }

    @Override
    public Document next()
    {
        return nextDoc;
    }

    /**
     * Not supported. Throws always a runtime exception.
     */
    @Override
    public void remove()
    {
        throw new RuntimeException("Not supported.");
    }

    @Override
    public void close()
    {
        documents.close();
        config = null;
        searchServerConfig = null;
    }

    @Override
    public int size()
    {
        return documents.size();
    }

    @Override
    public SearchServerResponseInfo getExtraInfo()
    {
        return documents.getExtraInfo();
    }

    @Override
    public void setExtraInfo(SearchServerResponseInfo info)
    {
        documents.setExtraInfo(info);
    }
}
