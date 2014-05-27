package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;

/**
 * Transforms a search result into json.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultJsonResponseRenderer implements
	Initiable<ResponseRendererFactory>, ResponseRendererIfc
{
	public static class Factory
	{
		public static DefaultJsonResponseRenderer getInstance()
		{
			return new DefaultJsonResponseRenderer();
		}
	}

	@Override
	public void init(ResponseRendererFactory config)
	{
		// TODO Auto-generated method stub

	}
}
