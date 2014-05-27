package org.outermedia.solrfusion.adapter.solr;

import lombok.ToString;

import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

/**
 * This class is able to send requests to a solr server and to receive answers.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultSolrAdapter implements Initiable<SearchServerConfig>,
	SearchServerAdapterIfc
{

	public static class Factory
	{
		public static DefaultSolrAdapter getInstance()
		{
			return new DefaultSolrAdapter();
		}
	}

	@Override
	public void init(SearchServerConfig config)
	{
		// TODO Auto-generated method stub

	}

}
