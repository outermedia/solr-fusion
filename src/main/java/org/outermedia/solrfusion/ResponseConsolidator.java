package org.outermedia.solrfusion;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.response.ClosableIterator;
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

    private ResponseConsolidator()
    {
        responseStreams = new ArrayList<>();
    }

    public void addResultStream(ClosableIterator<Document> docIterator)
    {
        responseStreams.add(docIterator);
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

    public ClosableIterator<Document> getResponseStream()
    {
        return new RoundRobinClosableIterator<Document>(responseStreams);
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
