package org.outermedia.solrfusion;

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

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.*;
import org.outermedia.solrfusion.adapter.solr.Solr1Adapter;
import org.outermedia.solrfusion.adapter.solr.Version;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.query.parser.MetaInfo;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The whole processing - handling a SolrFusion query until sending back a Solr response - is controlled by this class.
 * <p/>
 * Created by ballmann on 04.06.14.
 */
@Slf4j
public class FusionController implements FusionControllerIfc
{
    protected Configuration configuration;
    protected Util util;
    protected Throwable lastException;

    /**
     * Only factory creates instances.
     */
    protected FusionController()
    {
        util = new Util();
    }

    public static class Factory
    {
        public static FusionControllerIfc getInstance()
        {
            return new FusionController();
        }
    }

    @Override
    public void process(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse)
        throws InvocationTargetException, IllegalAccessException
    {
        this.configuration = configuration;

        processRequestWithQParam(configuration, fusionRequest, fusionResponse);
    }

    protected void processRequestWithQParam(Configuration configuration, FusionRequest fusionRequest,
        FusionResponse fusionResponse)
    {
        String queryStr = fusionRequest.getQuery().getValue();
        List<SolrFusionRequestParam> filterQueryList = fusionRequest.getFilterQuery();
        String highlightQueryStr = fusionRequest.getHighlightQuery().getValue();

        Map<String, Float> boosts = fusionRequest.getBoosts();
        Locale locale = fusionRequest.getLocale();
        fusionRequest.setParsedQuery(parseQuery(queryStr, boosts, locale, fusionRequest, true));
        List<String> unparsableFilterQueryStrings = new ArrayList<>();
        fusionRequest.setParsedFilterQuery(
            parseAllQueries(filterQueryList, boosts, fusionRequest, unparsableFilterQueryStrings));
        fusionRequest.setParsedHighlightQuery(parseQuery(highlightQueryStr, boosts, locale, fusionRequest, false));

        if (fusionRequest.getParsedQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(Arrays.asList(queryStr), fusionRequest.buildErrorMessage());
        }
        else if (unparsableFilterQueryStrings.size() > 0)
        {
            fusionResponse.setResponseForQueryParseError(unparsableFilterQueryStrings,
                fusionRequest.buildErrorMessage());
        }
        else if (highlightQueryStr != null && highlightQueryStr.trim().length() > 0 &&
            fusionRequest.getParsedHighlightQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(Arrays.asList(highlightQueryStr),
                fusionRequest.buildErrorMessage());
        }
        else
        {
            processQuery(configuration, fusionRequest, fusionResponse);
        }
    }

    protected List<ParsedQuery> parseAllQueries(List<SolrFusionRequestParam> queryParams, Map<String, Float> boosts,
        FusionRequest fusionRequest, List<String> unparsableQueryStrings)
    {
        List<ParsedQuery> result = null;
        if (queryParams != null)
        {
            result = new ArrayList<>();
            for (SolrFusionRequestParam sp : queryParams)
            {
                String queryStr = sp.getValue();
                Query q = parseQuery(queryStr, boosts, fusionRequest.getLocale(), fusionRequest, false);
                if (queryStr != null && queryStr.trim().length() > 0 && q == null)
                {
                    log.error("Ignoring filter query {}, because of parse errors.", queryStr);
                    unparsableQueryStrings.add(queryStr);
                }
                else
                {
                    result.add(new ParsedQuery(queryStr, q));
                }
            }
            if (result.isEmpty())
            {
                result = null;
            }
        }
        return result;
    }

    protected void processQuery(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse)
    {
        List<SearchServerConfig> configuredSearchServers = configuration.getConfigurationOfSearchServers();
        if (configuredSearchServers == null || configuredSearchServers.isEmpty())
        {
            fusionResponse.setResponseForNoSearchServerConfiguredError();
        }
        else
        {
            ResponseConsolidatorIfc consolidator = getNewResponseConsolidator();
            if (consolidator != null)
            {
                requestAllSearchServers(fusionRequest, configuredSearchServers, consolidator);
                if (consolidator.numberOfResponseStreams() < getDisasterLimit(configuration))
                {
                    fusionResponse.setResponseForTooLessServerAnsweredError(configuration.getDisasterMessage(),
                        consolidator.getErrorMsg());
                }
                else
                {
                    processResponses(fusionRequest, fusionResponse, consolidator);
                }
                consolidator.clear();
            }
            else
            {
                fusionResponse.setResponseForException(lastException);
            }
        }
    }

    protected int getDisasterLimit(Configuration configuration)
    {
        return configuration.getDisasterLimit();
    }

    protected ResponseConsolidatorIfc getNewResponseConsolidator()
    {
        try
        {
            return configuration.getResponseConsolidator(configuration);
        }
        catch (Exception e)
        {
            log.error("Unable to get a new ResponseConsolidatorIfc", e);
            lastException = e;
            return null;
        }
    }

    protected void processResponses(FusionRequest fusionRequest, FusionResponse fusionResponse,
        ResponseConsolidatorIfc consolidator)
    {
        try
        {
            ResponseRendererIfc responseRenderer = configuration.getResponseRendererByType(
                fusionRequest.getResponseType());
            if (responseRenderer == null)
            {
                fusionResponse.setResponseForMissingResponseRendererError(fusionRequest.getResponseType());
            }
            else
            {
                ClosableIterator<Document, SearchServerResponseInfo> response = consolidator.getResponseIterator(
                    fusionRequest);
                // set state BEFORE response is rendered, because their the status is read out! the query time too.
                fusionResponse.setOk();
                responseRenderer.writeResponse(configuration, response, fusionRequest, fusionResponse);
                response.close();
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while processing search server's responses", e);
            fusionResponse.setResponseForException(e);
        }
    }

    protected void requestAllSearchServers(FusionRequest fusionRequest,
        List<SearchServerConfig> configuredSearchServers, ResponseConsolidatorIfc consolidator)
    {
        log.debug("Requesting all configured servers with query: {}", fusionRequest.getQuery());
        ScriptEnv env = getNewScriptEnv(fusionRequest);
        Query query = fusionRequest.getParsedQuery();
        List<ParsedQuery> filterQuery = fusionRequest.getParsedFilterQuery();
        Query highlightQuery = fusionRequest.getParsedHighlightQuery();
        List<SolrFusionUriBuilderIfc> serverRequests = new ArrayList<>();

        // at first create all solr requests
        // because the query objects store the mapped values, it is not possible to map the queries in parallel
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            log.info("Processing query for search server {}", searchServerConfig.getSearchServerName());
            List<ParsedQuery> queryList = Arrays.asList(new ParsedQuery(fusionRequest.getQuery().getValue(), query));
            List<ParsedQuery> hlQueryList = Arrays.asList(
                new ParsedQuery(fusionRequest.getHighlightQuery().getValue(), highlightQuery));
            SolrFusionUriBuilderIfc ub = null;
            if (mapQuery(env, searchServerConfig, queryList, fusionRequest, QueryTarget.QUERY) &&
                mapQuery(env, searchServerConfig, hlQueryList, fusionRequest, QueryTarget.HIGHLIGHT_QUERY) &&
                mapQuery(env, searchServerConfig, filterQuery, fusionRequest, QueryTarget.FILTER_QUERY))
            {
                ub = createSolrRequest(fusionRequest, searchServerConfig);
                PostProcessorStatus status = searchServerConfig.applyQueryPostProcessors(
                    getNewPostProcessorScriptEnv(fusionRequest, searchServerConfig, ub, queryList.get(0),
                        hlQueryList.get(0), filterQuery));
                if (status == PostProcessorStatus.DO_NOT_SEND_QUERY)
                {
                    ub = null;
                }
            }
            else
            {
                log.info("Not sending request to {}, because mapping returned no value.",
                    searchServerConfig.getSearchServerName());
            }
            serverRequests.add(ub);
            getNewResetQueryState().reset(query);
            if (filterQuery != null)
            {
                getNewResetQueryState().reset(filterQuery);
            }
            if (highlightQuery != null)
            {
                getNewResetQueryState().reset(highlightQuery);
            }
        }

        // now request all solr servers in parallel!
        Collection<SolrRequestCallable> futures = new ArrayList<>();
        int serverNumber = serverRequests.size();
        for (int i = 0; i < serverNumber; i++)
        {
            SolrFusionUriBuilderIfc ub = serverRequests.get(i);
            if (ub != null)
            {
                futures.add(new SolrRequestCallable(ub, configuration, configuredSearchServers.get(i), consolidator,
                    fusionRequest));
            }
        }
        ExecutorService threadService = Executors.newFixedThreadPool(serverNumber);
        try
        {
            threadService.invokeAll(futures);
        }
        catch (InterruptedException e)
        {
            // NOP
        }
        threadService.shutdownNow();
    }

    protected ScriptEnv getNewScriptEnv(FusionRequest fusionRequest)
    {
        ScriptEnv env = new ScriptEnv();
        env.setConfiguration(configuration);
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, fusionRequest);
        return env;
    }

    protected ScriptEnv getNewPostProcessorScriptEnv(FusionRequest fusionRequest, SearchServerConfig searchServerConfig,
        SolrFusionUriBuilderIfc ub, ParsedQuery query, ParsedQuery hlQuery, List<ParsedQuery> filterQuery)
    {
        ScriptEnv env = new ScriptEnv();
        env.setConfiguration(configuration);
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, fusionRequest);
        env.setSearchServerConfig(searchServerConfig);
        env.setBinding(ScriptEnv.ENV_IN_SOLR_URL, ub);
        env.setBinding(ScriptEnv.ENV_IN_LOCALE, fusionRequest.getLocale());
        env.setBinding(ScriptEnv.ENV_IN_MAPPED_QUERY, query);
        env.setBinding(ScriptEnv.ENV_IN_MAPPED_HIGHLIGHT_QUERY, hlQuery);
        env.setBinding(ScriptEnv.ENV_IN_MAPPED_FILTER_QUERIES, filterQuery);
        return env;
    }

    protected ResetQueryState getNewResetQueryState()
    {
        return new ResetQueryState();
    }

    protected SolrFusionUriBuilderIfc createSolrRequest(FusionRequest fusionRequest,
        SearchServerConfig searchServerConfig)
    {
        SolrFusionUriBuilderIfc result = null;
        try
        {
            Multimap<String> searchServerParams = fusionRequest.buildSearchServerQueryParams(configuration,
                searchServerConfig);
            String searchServerQuery = searchServerParams.getFirst(SolrFusionRequestParams.QUERY);
            if (fusionRequest.getParsedQuery() != null &&
                (searchServerQuery == null || searchServerQuery.trim().length() == 0))
            {
                // the mapped query is empty which would return any documents, so don't ask this server
                log.debug("Don't request server {}, because query is empty.", searchServerConfig.getSearchServerName());
            }
            else
            {
                String queryType = searchServerParams.getFirst(SolrFusionRequestParams.QUERY_TYPE);
                SearchServerAdapterIfc<?> adapter = searchServerConfig.getInstance();
                boolean forDismax = false;
                if (MetaInfo.DISMAX_PARSER.equals(queryType))
                {
                    if (!(adapter instanceof Solr1Adapter))
                    {
                        // TODO always use q.alt instead of q? or second dismax config option needed?
                        String url = adapter.getUrl();
                        Version version = adapter.getSolrVersion();
                        adapter = Solr1Adapter.Factory.getInstance();
                        adapter.setUrl(url);
                        adapter.setSolrVersion(version);
                        ((Solr1Adapter) adapter).setSolrVersion(((Solr1Adapter) adapter).getSolrVersion());
                    }
                    forDismax = true;
                }
                result = adapter.buildHttpClientParams(configuration, searchServerConfig, fusionRequest,
                    searchServerParams, new Version(searchServerConfig.getSearchServerVersion()));
                if (result != null)
                {
                    result.setBuiltForDismax(forDismax);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Creation of http request failed for server " + searchServerConfig.getSearchServerName(), e);
        }
        return result;
    }

    protected boolean mapQuery(ScriptEnv env, SearchServerConfig searchServerConfig, List<ParsedQuery> queryList,
        FusionRequest fusionRequest, QueryTarget target)
    {
        boolean result = true;
        if (queryList != null)
        {
            for (ParsedQuery parsedQuery : queryList)
            {
                if (parsedQuery != null && parsedQuery.getQuery() != null)
                {
                    try
                    {
                        ScriptEnv newEnv = new ScriptEnv(env);
                        newEnv.setBinding(ScriptEnv.ENV_DISMAX_WORD_CACHE, new HashSet<String>());
                        configuration.getQueryMapper().mapQuery(configuration, searchServerConfig,
                            parsedQuery.getQuery(), newEnv, fusionRequest, target);
                    }
                    catch (Exception e)
                    {
                        log.error("Caught exception while mapping fusion query to search server query of server {}",
                            searchServerConfig.getSearchServerName(), e);
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    protected Query parseQuery(String query, Map<String, Float> boosts, Locale locale, FusionRequest fusionRequest,
        boolean checkQueryTypeParam)
    {
        Query queryObj = null;
        if (query != null)
        {
            try
            {
                QueryParserIfc queryParser = null;
                if (checkQueryTypeParam && fusionRequest.isDismaxQueryType())
                {
                    queryParser = configuration.getDismaxQueryParser();
                }
                else
                {
                    queryParser = configuration.getQueryParser();
                }
                try
                {
                    queryObj = queryParser.parse(configuration, boosts, query, locale, null);
                }
                catch (Exception e)
                {
                    String msg = "Parsing of query " + query + " failed.";
                    log.error(msg, e);
                    fusionRequest.addError(msg, e);
                }
            }
            catch (Exception e)
            {
                String msg = "Caught exception while parsing query: " + query;
                log.error(msg, e);
                fusionRequest.addError(msg, e);
            }
        }
        return queryObj;
    }

    @Override
    public void init(ControllerFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

}