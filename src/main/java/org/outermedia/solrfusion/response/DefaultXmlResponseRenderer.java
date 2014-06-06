package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Transforms a search result into xml.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultXmlResponseRenderer implements ResponseRendererIfc
{
	/**
	 * Factory creates instances only.
	 */
	private DefaultXmlResponseRenderer()
	{}

    @Override
    public String getResponseString(ClosableIterator<Document> docStream)
    {
        return null; // TODO
    }

    public static class Factory
	{
		public static DefaultXmlResponseRenderer getInstance()
		{
			return new DefaultXmlResponseRenderer();
		}
	}

	@Override
	public void init(ResponseRendererFactory config)
	{
		// TODO Auto-generated method stub

	}
}
