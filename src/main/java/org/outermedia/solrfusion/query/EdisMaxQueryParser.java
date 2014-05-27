package org.outermedia.solrfusion.query;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.QueryParserFactory;

/**
 * A common solr query parser.
 * 
 * @author ballmann
 * 
 */

@ToString
public class EdisMaxQueryParser implements Initiable<QueryParserFactory>,
	QueryParserIfc
{
	public static class Factory
	{
		public static EdisMaxQueryParser getInstance()
		{
			return new EdisMaxQueryParser();
		}
	}

	@Override
	public void init(QueryParserFactory config)
	{
		// TODO Auto-generated method stub

	}

}
