package org.outermedia.solrfusion;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.adapter.solr.Solr1Adapter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ControllerFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.query.parser.MetaInfo;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
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
                if (consolidator.numberOfResponseStreams() < configuration.getDisasterLimit())
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
                // TODO better to pass in a Writer in order to avoid building of very big String
                String responseString = responseRenderer.getResponseString(configuration, response, fusionRequest,
                    fusionResponse);
                fusionResponse.setOkResponse(responseString);
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
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            log.info("Processing query for search server {}", searchServerConfig.getSearchServerName());
            List<ParsedQuery> parsedQueries = Arrays.asList(new ParsedQuery(fusionRequest.getQuery().getValue(), query),
                new ParsedQuery(fusionRequest.getHighlightQuery().getValue(), highlightQuery));
            if (mapQuery(env, searchServerConfig, parsedQueries, fusionRequest) &&
                mapQuery(env, searchServerConfig, filterQuery, fusionRequest))
            {
                XmlResponse result = sendAndReceive(false, fusionRequest, searchServerConfig);
                Exception se = result.getErrorReason();
                if (se == null)
                {
                    SearchServerResponseInfo info = new SearchServerResponseInfo(result.getNumFound(), null, null,
                        null);
                    ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
                        result.getDocuments(), info);
                    consolidator.addResultStream(searchServerConfig, docIterator, fusionRequest,
                        result.getHighlighting(), result.getFacetFields(searchServerConfig.getIdFieldName(), 1));
                }
                else
                {
                    consolidator.addErrorResponse(se);
                }
            }
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
    }

    protected ScriptEnv getNewScriptEnv(FusionRequest fusionRequest)
    {
        ScriptEnv env = new ScriptEnv();
        env.setConfiguration(configuration);
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, fusionRequest);
        return env;
    }

    protected ResetQueryState getNewResetQueryState()
    {
        return new ResetQueryState();
    }

    protected XmlResponse sendAndReceive(boolean isIdQuery, FusionRequest fusionRequest,
        SearchServerConfig searchServerConfig)
    {
        try
        {
            XmlResponse result;
            Multimap<String> searchServerParams = fusionRequest.buildSearchServerQueryParams(configuration,
                searchServerConfig);
            String searchServerQuery = searchServerParams.getFirst(SolrFusionRequestParams.QUERY);
            if (fusionRequest.getParsedQuery() != null &&
                (searchServerQuery == null || searchServerQuery.trim().length() == 0))
            {
                // the mapped query is empty which would return any documents, so don't ask this server
                result = new XmlResponse();
                log.info("Ignoring server {}, because the mapped query is empty.",
                    searchServerConfig.getSearchServerName());
            }
            else
            {
                int timeout = configuration.getSearchServerConfigs().getTimeout();
                String queryType = searchServerParams.getFirst(SolrFusionRequestParams.QUERY_TYPE);
                SearchServerAdapterIfc adapter = searchServerConfig.getInstance();
                if (MetaInfo.DISMAX_PARSER.equals(queryType))
                {
                    adapter = newSolr1Adapter(adapter.getUrl());
                }
                InputStream is = adapter.sendQuery(configuration, searchServerConfig, fusionRequest, searchServerParams,
                    timeout, searchServerConfig.getSearchServerVersion());
                ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                    configuration.getDefaultResponseParser());
                result = responseParser.parse(is);
                if (result == null)
                {
                    result = new XmlResponse();
                    result.setErrorReason(new RuntimeException("Solr response parsing failed."));
                }
                if (log.isDebugEnabled())
                {
                    int docNr = -1;
                    int maxDocNr = -1;
                    if (result != null)
                    {
                        if (result.getDocuments() != null)
                        {
                            docNr = result.getDocuments().size();
                        }
                        maxDocNr = result.getNumFound();
                    }
                    log.debug("Received from {}: {} of max {}", searchServerConfig.getSearchServerName(), docNr,
                        maxDocNr);
                }
                if (log.isTraceEnabled())
                {
                    log.trace("Received from {}: {}", searchServerConfig.getSearchServerName(), result.toString());
                }
            }
            return result;
        }
        catch (SearchServerResponseException se)
        {
            return handleSearchServerResponseException(searchServerConfig, se);
        }
        catch (Exception e)
        {
            return handleGeneralResponseException(searchServerConfig, e);
        }
    }

    private XmlResponse handleGeneralResponseException(SearchServerConfig searchServerConfig, Exception e)
    {
        log.error("Caught exception while communicating with server " + searchServerConfig.getSearchServerName(), e);
        XmlResponse responseError = new XmlResponse();
        responseError.setErrorReason(e);
        return responseError;
    }

    private XmlResponse handleSearchServerResponseException(SearchServerConfig searchServerConfig,
        SearchServerResponseException se)
    {
        log.error("Caught exception while communicating with server " + searchServerConfig.getSearchServerName(), se);

        // try to parse error response if present
        try
        {
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            XmlResponse responseError = responseParser.parse(se.getHttpContent());
            if (responseError != null)
            {
                se.setResponseError(responseError.getResponseErrors());
                responseError.setErrorReason(se);
                return responseError;
            }
        }
        catch (Exception e)
        {
            // depending on solr's version, a well formed error message is provided or not
            log.warn("Couldn't parse error response", e);
        }
        XmlResponse responseError = new XmlResponse();
        responseError.setErrorReason(se);
        return responseError;
    }

    protected SearchServerAdapterIfc newSolr1Adapter(String url)
    {
        SearchServerAdapterIfc result = Solr1Adapter.Factory.getInstance();
        result.setUrl(url);
        return result;
    }

    protected boolean mapQuery(ScriptEnv env, SearchServerConfig searchServerConfig, List<ParsedQuery> queryList,
        FusionRequest fusionRequest)
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
                        configuration.getQueryMapper().mapQuery(configuration, searchServerConfig,
                            parsedQuery.getQuery(), env, fusionRequest);
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