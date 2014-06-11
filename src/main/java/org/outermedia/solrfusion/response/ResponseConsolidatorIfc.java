package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ResponseConsolidatorIfc extends Initiable<ResponseConsolidatorFactory>
{

    public void addResultStream(Configuration config, SearchServerConfig searchServerConfig,
                                ClosableIterator<Document> docIterator);

    public int numberOfResponseStreams();

    public void reset();

    public ClosableIterator<Document> getResponseIterator();

}
