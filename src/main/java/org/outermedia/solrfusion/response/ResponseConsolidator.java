package org.outermedia.solrfusion.response;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
@ToString
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
        responseStreams.add(getNewMappingClosableIterator(config, searchServerConfig, docIterator));
    }

    protected MappingClosableIterator getNewMappingClosableIterator(Configuration config,
            SearchServerConfig searchServerConfig, ClosableIterator<Document> docIterator)
    {
        return new MappingClosableIterator(docIterator, config, searchServerConfig);
    }

    public int numberOfResponseStreams()
    {
        return responseStreams.size();
    }

    public void reset()
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
