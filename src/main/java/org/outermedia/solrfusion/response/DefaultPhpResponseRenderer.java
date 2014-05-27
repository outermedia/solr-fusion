package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;

/**
 * Transforms a search result into php.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultPhpResponseRenderer implements
	Initiable<ResponseRendererFactory>, ResponseRendererIfc
{
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
