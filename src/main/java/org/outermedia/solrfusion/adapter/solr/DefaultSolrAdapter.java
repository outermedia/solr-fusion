package org.outermedia.solrfusion.adapter.solr;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.response.parser.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author ballmann
 */

@ToString
@Slf4j
public class DefaultSolrAdapter implements SearchServerAdapterIfc<SolrFusionUriBuilder>
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
    public String QUERY_TYPE_PARAMETER = "qt";
    public String BOOST_PARAMETER = "qf";
    public String MIN_MATCH_PARAMETER = "mm";

    @Setter @Getter
    private String url;

    @Setter @Getter
    protected Version solrVersion;

    /**
     * Factory creates instances only.
     */
    protected DefaultSolrAdapter()
    {
    }

    @Override
    public InputStream sendQuery(SolrFusionUriBuilder uriBuilder, int timeout) throws URISyntaxException, IOException
    {
        HttpClient client = newHttpClient();

        if (log.isDebugEnabled())
        {
            log.debug("Sending query {}", uriBuilder.build());
        }

        HttpPost request = newHttpPost(url, uriBuilder);
        RequestConfig requestConfig = newRequestConfig(timeout);

        request.setConfig(requestConfig);

        long startTime = System.currentTimeMillis();
        HttpResponse response = client.execute(request);
        long endTime = System.currentTimeMillis();

        StatusLine statusLine = response.getStatusLine();
        int httpStatusCode = statusLine.getStatusCode();
        String reason = statusLine.getReasonPhrase();

        log.debug("Received response in {}ms from host {}: {}", endTime-startTime, url, statusLine.toString());

        InputStream contentStream = response.getEntity().getContent();

        if (httpStatusCode != 200)
        {
            throw new SearchServerResponseException(httpStatusCode, reason, contentStream);
        }

        return contentStream;
    }

    protected Double parseDouble(String version)
    {
        Double result = null;
        try
        {
            result = Double.valueOf(version);
        }
        catch (Exception e)
        {
            log.error("Can't parse search server version '{}'", version, e);
        }

        return result;
    }

    @Override
    public SolrFusionUriBuilder buildHttpClientParams(Configuration configuration,
        SearchServerConfig searchServerConfig, FusionRequest fusionRequest, Multimap<String> params, Version version)
        throws URISyntaxException
    {
        this.solrVersion = version;
        SolrFusionUriBuilder ub = new SolrFusionUriBuilder(url);
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

        addIfNotNull(ub, START_PARAMETER, params.getFirst(START));
        addIfNotNull(ub, ROWS_PARAMETER, params.getFirst(PAGE_SIZE));
        addIfNotNull(ub, SORT_PARAMETER, params.getFirst(SORT));
        addIfNotNull(ub, BOOST_PARAMETER, params.getFirst(QUERY_FIELD));
        addIfNotNull(ub, MIN_MATCH_PARAMETER, params.getFirst(MINIMUM_MATCH));

        ub.setParameter(FIELDS_TO_RETURN_PARAMETER, params.getFirst(FIELDS_TO_RETURN));
        String queryType = params.getFirst(QUERY_TYPE);
        if (queryType != null)
        {
            ub.setParameter(QUERY_TYPE_PARAMETER, queryType);
        }

        buildHighlightHttpClientParams(params, ub);

        buildFacetHttpClientParams(params, ub);

        if (!"q".equals(QUERY_PARAMETER))
        {
            // create dismax query to get highlights too
            try
            {
                Set<String> defaultSearchFields = fusionRequest.mapFusionFieldToSearchServerField(
                    configuration.getDefaultSearchField(), configuration, searchServerConfig, null, QueryTarget.QUERY);
                QueryBuilderIfc dismaxQueryBuilder = configuration.getDismaxQueryBuilder();
                Locale locale = fusionRequest.getLocale();
                String qs = dismaxQueryBuilder.buildQueryString(fusionRequest.getParsedQuery(), configuration,
                    searchServerConfig, locale, defaultSearchFields, QueryTarget.QUERY);
                log.debug("Setting q to dismax query in order to get highlights. Step 1: {}", qs);
                qs = dismaxQueryBuilder.getStaticallyAddedQueries(configuration, searchServerConfig, locale,
                    QueryTarget.QUERY, qs);
                ub.setParameter("q", qs);
                log.debug("Setting q to dismax query in order to get highlights. Step 2: {}", qs);
            }
            catch (Exception e)
            {
                log.error("Caught exception while creating dismax query", e);
            }
        }

        return ub;
    }

    protected void addIfNotNull(URIBuilder ub, String param, String value)
    {
        if (value != null)
        {
            ub.setParameter(param, value);
        }
    }

    protected void buildFacetHttpClientParams(Multimap<String> params, URIBuilder ub)
    {
        String facet = params.getFirst(FACET);
        if ("true".equals(facet))
        {
            ub.setParameter(FACET_PARAMETER, "true");
            addIfNotNull(ub, FACET_SORT_PARAMETER, params.getFirst(FACET_SORT));
            addIfNotNull(ub, FACET_PREFIX_PARAMETER, params.getFirst(FACET_PREFIX));
            addIfNotNull(ub, FACET_MINCOUNT_PARAMETER, params.getFirst(FACET_MINCOUNT));
            addIfNotNull(ub, FACET_LIMIT_PARAMETER, params.getFirst(FACET_LIMIT));
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
            addIfNotNull(ub, HL_SIMPLE_PRE_PARAMETER, params.getFirst(HIGHLIGHT_PRE));
            addIfNotNull(ub, HL_SIMPLE_POST_PARAMETER, params.getFirst(HIGHLIGHT_POST));
            addIfNotNull(ub, HL_FIELDS_PARAMETER, params.getFirst(HIGHLIGHT_FIELDS_TO_RETURN));
            addIfNotNull(ub, HL_QUERY_PARAMETER, params.getFirst(HIGHLIGHT_QUERY));
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

    protected HttpPost newHttpPost(String url, SolrFusionUriBuilderIfc ub) throws URISyntaxException, UnsupportedEncodingException
    {
        HttpPost result = new HttpPost(url);
        List<NameValuePair> params = ub.getQueryParams();
        result.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));

        return result;
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
        if (config.getQueryParamName() != null)
        {
            QUERY_PARAMETER = config.getQueryParamName();
            log.debug("Using special query parameter '{}'", QUERY_PARAMETER);
        }
    }

    @Override public void finish() throws Exception
    {
        // NOP
    }

    @Override public void commitLastDocs()
    {
        throw new RuntimeException("Not implemented.");
    }

    @Override public void add(Document doc) throws Exception
    {
        throw new RuntimeException("Not implemented.");
    }

    @Override public void deleteByQuery(String query) throws Exception
    {
        throw new RuntimeException("Not implemented.");
    }

}
