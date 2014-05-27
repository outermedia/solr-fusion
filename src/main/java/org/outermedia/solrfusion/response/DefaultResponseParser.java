package org.outermedia.solrfusion.response;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;

/**
 * Parses a solr server's xml response into an internal representation.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultResponseParser implements Initiable<ResponseParserFactory>,
	ResponseParserIfc
{
	public static class Factory
	{
		public static DefaultResponseParser getInstance()
		{
			return new DefaultResponseParser();
		}
	}

	@Override
	public void init(ResponseParserFactory config)
	{
		// TODO Auto-generated method stub

	}

}
