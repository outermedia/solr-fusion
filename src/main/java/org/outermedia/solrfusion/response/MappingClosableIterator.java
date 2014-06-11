package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 6/11/14.
 */
public class MappingClosableIterator implements ClosableIterator<Document>
{
    private Configuration config;
    private SearchServerConfig searchServerConfig;
    private ResponseMapperIfc responseMapper;
    private ClosableIterator<Document> documents;

    public MappingClosableIterator(ClosableIterator<Document> documents, Configuration config,
                                   SearchServerConfig searchServerConfig)
    {
        responseMapper = config.getResponseMapper();
        this.config = config;
        this.documents = documents;
        this.searchServerConfig = searchServerConfig;
    }

    @Override
    public boolean hasNext()
    {
        return documents.hasNext();
    }

    @Override
    public Document next()
    {
        Document d = documents.next();
        if (d != null)
        {
            ScriptEnv env = new ScriptEnv();
            responseMapper.mapResponse(config, searchServerConfig, d, env);
        }
        return d;
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
}
