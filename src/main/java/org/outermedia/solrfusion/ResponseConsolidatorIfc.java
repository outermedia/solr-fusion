package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ResponseConsolidatorIfc extends Initiable<ResponseConsolidatorFactory>
{

    public void addResultStream(ClosableIterator<Document> docIterator);

    public int numberOfResponseStreams();

    public void reset();

    public ClosableIterator<Document> getResponseStream();
}
