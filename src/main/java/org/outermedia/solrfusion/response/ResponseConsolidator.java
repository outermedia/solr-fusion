package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
@ToString
@Slf4j
public class ResponseConsolidator implements ResponseConsolidatorIfc
{
    private List<ClosableIterator<Document>> responseStreams;

    /**
     * Factory creates instances only.
     */
    private ResponseConsolidator()
    {
        responseStreams = new ArrayList<>();
    }

    @Override
    public void addResultStream(Configuration config, SearchServerConfig searchServerConfig, ClosableIterator<Document> docIterator)
    {
        try
        {
            responseStreams.add(getNewMappingClosableIterator(config, searchServerConfig, docIterator));
        }
        catch (Exception e)
        {
            log.error("Caught exception while adding document responses of server {}", searchServerConfig.getSearchServerName(), e);
        }
    }

    protected MappingClosableIterator getNewMappingClosableIterator(Configuration config,
            SearchServerConfig searchServerConfig, ClosableIterator<Document> docIterator)
            throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, config, searchServerConfig);
    }

    public int numberOfResponseStreams()
    {
        return responseStreams.size();
    }

    public void clear()
    {
        for (ClosableIterator<Document> docIterator : responseStreams)
        {
            docIterator.close();
        }
        responseStreams.clear();
    }

    @Override
    public ClosableIterator<Document> getResponseIterator()
    {
        return new RoundRobinClosableIterator<>(responseStreams);
    }

    public static class Factory
    {
        public static ResponseConsolidator getInstance()
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
