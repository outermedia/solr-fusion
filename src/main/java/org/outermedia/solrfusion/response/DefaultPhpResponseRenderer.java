package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a search result into php.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultPhpResponseRenderer implements ResponseRendererIfc
{

	/**
	 * Factory creates instances only.
	 */
	private DefaultPhpResponseRenderer()
	{}

    @Override
    public String getResponseString(ClosableIterator<Document,SearchServerResponseInfo> docStream, String query)
    {
        return null; // TODO
    }

    public static class Factory
	{
		public static DefaultPhpResponseRenderer getInstance()
		{
			return new DefaultPhpResponseRenderer();
		}
	}

	@Override
	public void init(ResponseRendererFactory config)
	{
		// TODO Auto-generated method stub

	}
}
