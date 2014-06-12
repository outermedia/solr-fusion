package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Allows to send and receive data from a search server.
 * 
 * @author ballmann
 * 
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
    public java.io.InputStream sendQuery(String searchServerQueryStr) throws URISyntaxException, IOException;

    public void init(SearchServerConfig config);
}
