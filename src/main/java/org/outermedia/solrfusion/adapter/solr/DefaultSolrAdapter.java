package org.outermedia.solrfusion.adapter.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
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
@Slf4j
public class DefaultSolrAdapter implements SearchServerAdapterIfc
{

    private final int DEFAULT_TIMEOUT = 4000;
    private final String QUERY_PARAMETER = "q";
    private final String WRITER_TYPE_PARAMETER = "wt";
    private final String DEFAULT_WRITER_TYPE_PARAMETER = "xml";

    private String url;

    /**
	 * Factory creates instances only.
	 */
	private DefaultSolrAdapter()
	{}

    @Override
    public InputStream sendQuery(String searchServerQueryStr) throws URISyntaxException, IOException
    {
        return sendQuery(searchServerQueryStr, DEFAULT_TIMEOUT);
    }

    @Override
    public InputStream sendQuery(String searchServerQueryStr, int timeout) throws URISyntaxException, IOException
    {
        URIBuilder ub = new URIBuilder(url);
        ub.setParameter(QUERY_PARAMETER, searchServerQueryStr);
        ub.setParameter(WRITER_TYPE_PARAMETER, DEFAULT_WRITER_TYPE_PARAMETER);

        log.debug("Sending query to host {}: {}", url, searchServerQueryStr);

        HttpClient client = HttpClientBuilder.create().build();

        HttpGet request = new HttpGet(ub.build());
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();

        request.setConfig(requestConfig);
        HttpResponse response = client.execute(request);

        log.debug("Received response from host {}: {}", url, response.getStatusLine().toString());

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
        url = config.getUrl();
    }

}
