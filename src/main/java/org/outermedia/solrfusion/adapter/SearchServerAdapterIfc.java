package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Allows to send and receive data from a search server.
 *
 * @author ballmann
 */

public interface SearchServerAdapterIfc extends Initiable<SearchServerConfig>
{
    /**
     * Send the provided query to a search server and returns the retrieved documents. The adapter creates an
     * instance of {@link org.outermedia.solrfusion.adapter.SearchServerResponseInfo} and sets it in the returned
     * ClosableIterator.
     *
     * @param searchServerQueryStr a query suitable for this search server.
     * @return null for error or a document stream where
     * {@link org.outermedia.solrfusion.response.ClosableIterator#setExtraInfo(Object)} has been called.
     */
    public ClosableIterator<Document, SearchServerResponseInfo> sendQuery(String searchServerQueryStr);

    public void init(SearchServerConfig config);
}
