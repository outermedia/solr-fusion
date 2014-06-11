package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a search result into a transport format.
 * 
 * @author ballmann
 * 
 */

public interface ResponseRendererIfc extends Initiable<ResponseRendererFactory>
{
	public String getResponseString(ClosableIterator<Document> docStream, String query);
}
