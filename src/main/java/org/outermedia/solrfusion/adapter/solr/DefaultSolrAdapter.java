package org.outermedia.solrfusion.adapter.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.outermedia.solrfusion.SolrFusionRequestParams;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author ballmann
 */

@ToString
@Slf4j
public class DefaultSolrAdapter implements SearchServerAdapterIfc
{

    private final int DEFAULT_TIMEOUT = 4000;
    private final String QUERY_PARAMETER = "q";
    private final String FILTER_QUERY_PARAMETER = "fq";
    private final String WRITER_TYPE_PARAMETER = "wt";
    private final String DEFAULT_WRITER_TYPE_PARAMETER = "xml";

    private String url;

    /**
     * Factory creates instances only.
     */
    private DefaultSolrAdapter()
    {
    }

    @Override
    public InputStream sendQuery(Map<String, String> params, int timeout) throws URISyntaxException, IOException
    {
        URIBuilder ub = new URIBuilder(url);
        String q = params.get(SolrFusionRequestParams.QUERY.getRequestParamName());
        ub.setParameter(QUERY_PARAMETER, q);
        String fq = params.get(SolrFusionRequestParams.FILTER_QUERY.getRequestParamName());
        if (fq != null)
        {
            ub.setParameter(FILTER_QUERY_PARAMETER, fq);
        }
        ub.setParameter(WRITER_TYPE_PARAMETER, DEFAULT_WRITER_TYPE_PARAMETER);

        log.debug("Sending query to host {}: q={} fq={}", url, q, fq);

        HttpClient client = newHttpClient();

        HttpGet request = newHttpGet(ub);
        RequestConfig requestConfig = newRequestConfig(timeout);

        request.setConfig(requestConfig);
        HttpResponse response = client.execute(request);

        StatusLine statusLine = response.getStatusLine();
        int httpStatusCode = statusLine.getStatusCode();
        String reason = statusLine.getReasonPhrase();

        log.debug("Received response from host {}: {}", url, statusLine.toString());

        InputStream contentStream = response.getEntity().getContent();

        if (httpStatusCode != 200)
        {
            throw new SearchServerResponseException(httpStatusCode, reason, contentStream);
        }

        return contentStream;
    }

    protected CloseableHttpClient newHttpClient()
    {
        return HttpClientBuilder.create().build();
    }

    protected RequestConfig newRequestConfig(int timeout)
    {
        return RequestConfig.custom()
            .setSocketTimeout(timeout)
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .build();
    }

    protected HttpGet newHttpGet(URIBuilder ub) throws URISyntaxException
    {
        return new HttpGet(ub.build());
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
        url = config.getUrl();
    }

}
