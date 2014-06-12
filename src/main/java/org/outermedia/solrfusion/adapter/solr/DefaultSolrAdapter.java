package org.outermedia.solrfusion.adapter.solr;

import lombok.ToString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * This class is able to send requests to a solr server and to receive answers.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultSolrAdapter implements SearchServerAdapterIfc
{

    private final String QUERY_PARAMETER = "q";

    private String url;

    /**
	 * Factory creates instances only.
	 */
	private DefaultSolrAdapter()
	{}

    @Override
    public InputStream sendQuery(String searchServerQueryStr) throws URISyntaxException, IOException
    {
        URIBuilder ub = new URIBuilder(url);
        ub.setParameter(QUERY_PARAMETER, searchServerQueryStr);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(ub.build());
        HttpResponse response = client.execute(request);

        return response.getEntity().getContent();
    }

    public static class Factory
	{
		public static DefaultSolrAdapter getInstance()
		{
			return new DefaultSolrAdapter();
		}
	}

	@Override
	public void init(SearchServerConfig config) {
		// TODO Auto-generated method stub
        url = config.getUrl();
    }

}
