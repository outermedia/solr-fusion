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
    public ClosableIterator<Document> sendQuery(String searchServerQueryStr);

    public void init(SearchServerConfig config);
}
