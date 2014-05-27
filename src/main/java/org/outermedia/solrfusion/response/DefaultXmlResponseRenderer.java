package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;

/**
 * Transforms a search result into xml.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultXmlResponseRenderer implements
	Initiable<ResponseRendererFactory>, ResponseRendererIfc
{
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
