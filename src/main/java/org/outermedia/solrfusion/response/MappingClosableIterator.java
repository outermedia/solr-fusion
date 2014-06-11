package org.outermedia.solrfusion.response;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ballmann on 6/11/14.
 */
@Slf4j
public class MappingClosableIterator implements ClosableIterator<Document>
{
    private Configuration config;
    private SearchServerConfig searchServerConfig;
    private ResponseMapperIfc responseMapper;
    private ClosableIterator<Document> documents;

    private boolean ignoreUnmappedFields = true;
    private Document nextDoc;

    public MappingClosableIterator(ClosableIterator<Document> documents, Configuration config,
            SearchServerConfig searchServerConfig) throws InvocationTargetException, IllegalAccessException
    {
        responseMapper = config.getResponseMapper();
        this.config = config;
        this.documents = documents;
        this.searchServerConfig = searchServerConfig;
    }

    @Override
    public boolean hasNext()
    {
        while (documents.hasNext())
        {
            nextDoc = documents.next();
            ScriptEnv env = new ScriptEnv();
            try
            {
                responseMapper.mapResponse(config, searchServerConfig, nextDoc, env);
                // TODO if nextDoc contains no mapped fields ignore it!
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
    }

    @Override
    public int size()
    {
        return documents.size();
    }
}
