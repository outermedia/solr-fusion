package org.outermedia.solrfusion;

import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class ResponseConsolidator
{
    private List<ClosableIterator<Document>> responseStreams;

    public ResponseConsolidator()
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

    // TODO should be a factory!
}
