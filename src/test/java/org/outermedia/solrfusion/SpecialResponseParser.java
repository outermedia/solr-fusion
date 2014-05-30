package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;

/**
 * Dummy implementation for unit test.
 * 
 * @author ballmann
 * 
 */

@ToString
public class SpecialResponseParser implements Initiable<ResponseParserFactory>
{
	private SpecialResponseParser()
	{}

	public static class Factory
	{
		public static Object getInstance()
		{
			return new SpecialResponseParser();
		}
	}

	@Override
	public void init(ResponseParserFactory config)
	{
		// NOP
	}

}
