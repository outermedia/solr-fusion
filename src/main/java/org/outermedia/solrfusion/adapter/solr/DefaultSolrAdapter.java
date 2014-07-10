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
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringTokenizer;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author ballmann
 */

@ToString
@Slf4j
public class DefaultSolrAdapter implements SearchServerAdapterIfc
{
    public final static String QUERY_PARAMETER = "q";
    public final static String FILTER_QUERY_PARAMETER = "fq";
    public final static String WRITER_TYPE_PARAMETER = "wt";
    public final static String DEFAULT_WRITER_TYPE_PARAMETER = "xml";
    public final static String START_PARAMETER = "start";
    public final static String ROWS_PARAMETER = "rows";
    public final static String SORT_PARAMETER = "sort";
    public final static String FIELDS_TO_RETURN_PARAMETER = "fl";

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
        String q = params.get(QUERY.getRequestParamName());
        ub.setParameter(QUERY_PARAMETER, q);
        String fq = params.get(FILTER_QUERY.getRequestParamName());
        if (fq != null)
        {
            ub.setParameter(FILTER_QUERY_PARAMETER, fq);
        }
        String responseFormat = params.get(WRITER_TYPE.getRequestParamName());
        ub.setParameter(WRITER_TYPE_PARAMETER, responseFormat);
        String start = params.get(START.getRequestParamName());
        ub.setParameter(START_PARAMETER, start);
        String rows = params.get(PAGE_SIZE.getRequestParamName());
        ub.setParameter(ROWS_PARAMETER, rows);
        String sortStr = params.get(SORT.getRequestParamName());
        ub.setParameter(SORT_PARAMETER, sortStr);
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        String sortField = st.nextToken();
        String sortDir = st.nextToken();
        ub.setParameter(FIELDS_TO_RETURN_PARAMETER, "* " + sortField);

        log.debug("Sending query to host {}: q={} fq={} start={} rows={} sort={}", url, q, fq, start, rows, sortField);

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
        return RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).setConnectionRequestTimeout(
            timeout).build();
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
