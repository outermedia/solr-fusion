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
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

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
    public String QUERY_PARAMETER = "q";
    public String FILTER_QUERY_PARAMETER = "fq";
    public String WRITER_TYPE_PARAMETER = "wt";
    public String START_PARAMETER = "start";
    public String ROWS_PARAMETER = "rows";
    public String SORT_PARAMETER = "sort";
    public String FIELDS_TO_RETURN_PARAMETER = "fl";
    public String HL_SIMPLE_PRE_PARAMETER = "hl.simple.pre";
    public String HL_FIELDS_PARAMETER = "hl.fl";
    public String HL_ON_PARAMETER = "hl";
    public String HL_QUERY_PARAMETER = "hl.q";
    public String HL_SIMPLE_POST_PARAMETER = "hl.simple.post";
    public String FACET_PARAMETER = "facet";
    public String FACET_MINCOUNT_PARAMETER = "facet.mincount";
    public String FACET_LIMIT_PARAMETER = "facet.limit";
    public String FACET_FIELD_SORT_PATTERN_PARAMETER = "f.%s.facet.sort";
    public String FACET_SORT_PARAMETER = "facet.sort";
    public String FACET_PREFIX_PARAMETER = "facet.prefix";
    public String FACET_FIELD_PARAMETER = "facet.field";


    private String url;

    /**
     * Factory creates instances only.
     */
    protected DefaultSolrAdapter()
    {
    }

    @Override
    public InputStream sendQuery(Multimap<String> params, int timeout, String version) throws URISyntaxException, IOException
    {
        HttpClient client = newHttpClient();

        HttpGet request = newHttpGet(buildHttpClientParams(params));
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

    protected URIBuilder buildHttpClientParams(Multimap<String> params) throws URISyntaxException
    {
        URIBuilder ub = new URIBuilder(url);
        ub.setParameter(QUERY_PARAMETER, params.getFirst(QUERY));
        Collection<String> fqs = params.get(FILTER_QUERY);
        if (fqs != null)
        {
            for (String fq : fqs)
            {
                ub.addParameter(FILTER_QUERY_PARAMETER, fq);
            }
        }
        ub.setParameter(WRITER_TYPE_PARAMETER, params.getFirst(WRITER_TYPE));
        ub.setParameter(START_PARAMETER, params.getFirst(START));
        ub.setParameter(ROWS_PARAMETER, params.getFirst(PAGE_SIZE));
        String sortStr = params.getFirst(SORT);
        ub.setParameter(SORT_PARAMETER, sortStr);
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        String sortField = st.nextToken();
        ub.setParameter(FIELDS_TO_RETURN_PARAMETER, params.getFirst(FIELDS_TO_RETURN));

        buildHighlightHttpClientParams(params, ub);

        buildFacetHttpClientParams(params, ub);

        log.debug("Sending query {}", ub.build());

        return ub;
    }

    protected void buildFacetHttpClientParams(Multimap<String> params, URIBuilder ub)
    {
        String facet = params.getFirst(FACET);
        if ("true".equals(facet))
        {
            ub.setParameter(FACET_PARAMETER, "true");
            String facetSort = params.getFirst(FACET_SORT);
            if (facetSort != null)
            {
                ub.setParameter(FACET_SORT_PARAMETER, facetSort);
            }
            String facetPrefix = params.getFirst(FACET_PREFIX);
            if (facetPrefix != null)
            {
                ub.setParameter(FACET_PREFIX_PARAMETER, facetPrefix);
            }
            String facetMincount = params.getFirst(FACET_MINCOUNT);
            if (facetMincount != null)
            {
                ub.setParameter(FACET_MINCOUNT_PARAMETER, facetMincount);
            }
            String facetLimit = params.getFirst(FACET_LIMIT);
            if (facetLimit != null)
            {
                ub.setParameter(FACET_LIMIT_PARAMETER, facetLimit);
            }
            Collection<String> facetFields = params.get(FACET_FIELD);
            if (facetFields != null)
            {
                for (String ff : facetFields)
                {
                    ub.addParameter(FACET_FIELD_PARAMETER, ff);
                }
            }
            List<Map.Entry<String, String>> sortFields = params.filterBy(FACET_SORT_FIELD);
            for (Map.Entry<String, String> sfEntry : sortFields)
            {
                ub.addParameter(sfEntry.getKey(), sfEntry.getValue());
            }
        }
    }

    protected void buildHighlightHttpClientParams(Multimap<String> params, URIBuilder ub)
    {
        String doHighlighting = params.getFirst(HIGHLIGHT);
        if ("true".equals(doHighlighting))
        {
            ub.setParameter(HL_ON_PARAMETER, "true");
            String pre = params.getFirst(HIGHLIGHT_PRE);
            if (pre != null)
            {
                ub.setParameter(HL_SIMPLE_PRE_PARAMETER, pre);
            }
            String post = params.getFirst(HIGHLIGHT_POST);
            if (post != null)
            {
                ub.setParameter(HL_SIMPLE_POST_PARAMETER, post);
            }
            String fields = params.getFirst(HIGHLIGHT_FIELDS_TO_RETURN);
            if (fields != null)
            {
                ub.setParameter(HL_FIELDS_PARAMETER, fields);
            }
            String hlq = params.getFirst(HIGHLIGHT_QUERY);
            if (hlq != null)
            {
                ub.setParameter(HL_QUERY_PARAMETER, hlq);
            }
        }
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
        public static SearchServerAdapterIfc getInstance()
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
