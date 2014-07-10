package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Allows to send and receive data from a search server.
 * 
 * @author ballmann
 * 
 */

public interface SearchServerAdapterIfc extends Initiable<SearchServerConfig>
{
    /**
     * Send the provided query to a search server and returns the retrieved documents as InputStream.
     * In the case of an error a {@link org.outermedia.solrfusion.adapter.SearchServerResponseException} is thrown.
     *
     * @param searchServerQueryStr a query suitable for this search server.
     * @param timeout a timeout in milliseconds
     * @return null for error or a document stream
     */
    public InputStream sendQuery(Map<String, String> searchServerQueryStr, int timeout) throws URISyntaxException, IOException;

    public void init(SearchServerConfig config);
}
