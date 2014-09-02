package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Allows to send and receive data from a search server.
 *
 * @author ballmann
 */

public interface SearchServerAdapterIfc extends Initiable<SearchServerConfig>
{

    /**
     * Send the provided query to a search server and returns the retrieved documents as InputStream. In the case of an
     * error a {@link org.outermedia.solrfusion.adapter.SearchServerResponseException} is thrown.
     *
     *
     * @param configuration
     * @param searchServerConfig
     *@param fusionRequest
     * @param searchServerQueryStr a query suitable for this search server.
     * @param timeout              a timeout in milliseconds
     * @param version              the version of the search server     @return null for error or a document stream
     */
    public InputStream sendQuery(Configuration configuration, SearchServerConfig searchServerConfig,
        FusionRequest fusionRequest, Multimap<String> searchServerQueryStr, int timeout, String version)
        throws URISyntaxException, IOException;

    public void init(SearchServerConfig config);

    public void setUrl(String url);
    public String getUrl();
}
