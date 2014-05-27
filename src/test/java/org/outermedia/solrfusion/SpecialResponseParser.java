package org.outermedia.solrfusion;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;

@ToString
public class SpecialResponseParser implements Initiable<ResponseParserFactory>
{
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
		// TODO Auto-generated method stub

	}

}
