package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Allows to send and receive data from a search server.
 * 
 * @author ballmann
 * 
 */

public interface SearchServerAdapterIfc extends Initiable<SearchServerConfig>
{
    /**
     *  Send the provided query to a search server and return the retrieved documents.
     *
     * @param searchServerQueryStr a query suitable for this search server.
     * @return null for error or a document stream
     */
    public ClosableIterator<Document> sendQuery(String searchServerQueryStr);

    public void init(SearchServerConfig config);
}
