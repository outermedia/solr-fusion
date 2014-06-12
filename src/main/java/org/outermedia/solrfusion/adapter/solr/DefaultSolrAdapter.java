package org.outermedia.solrfusion.adapter.solr;

import lombok.ToString;

import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * This class is able to send requests to a solr server and to receive answers.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultSolrAdapter implements SearchServerAdapterIfc
{
	/**
	 * Factory creates instances only.
	 */
	private DefaultSolrAdapter()
	{}

    @Override
    public ClosableIterator<Document,SearchServerResponseInfo> sendQuery(String searchServerQueryStr)
    {
        return null; // TODO
    }

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
