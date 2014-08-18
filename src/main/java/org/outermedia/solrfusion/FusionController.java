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
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
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
        fusionRequest.setParsedQuery(parseQuery(queryStr, boosts, locale, fusionRequest));
        List<String> unparsableFilterQueryStrings = new ArrayList<>();
        fusionRequest.setParsedFilterQuery(
            parseAllQueries(filterQueryList, boosts, fusionRequest, unparsableFilterQueryStrings));
        fusionRequest.setParsedHighlightQuery(parseQuery(highlightQueryStr, boosts, locale, fusionRequest));

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

    protected List<Query> parseAllQueries(List<SolrFusionRequestParam> queryParams, Map<String, Float> boosts,
        FusionRequest fusionRequest, List<String> unparsableQueryStrings)
    {
        List<Query> result = null;
        if (queryParams != null)
        {
            result = new ArrayList<>();
            for (SolrFusionRequestParam sp : queryParams)
            {
                String queryStr = sp.getValue();
                Query q = parseQuery(queryStr, boosts, fusionRequest.getLocale(), fusionRequest);
                if (queryStr != null && queryStr.trim().length() > 0 && q == null)
                {
                    log.error("Ignoring filter query {}, because of parse errors.", queryStr);
                    unparsableQueryStrings.add(queryStr);
                }
                else
                {
                    result.add(q);
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
        else if (isIdQuery(fusionRequest.getParsedQuery()))
        {
            processIdQuery(fusionRequest, fusionResponse);
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

    /**
     * Ensures that processIdQuery() can send an id query. "q" has to be a TermQuery (subclass) with the id field name
     * configured in the fusion schema. The query must have only one value where the id generator must be able to
     * extract the search server's name and doc id. The fusion schema contains a section for the extract search server.
     *
     * @param q the query being tested
     * @return true if q is really an id query, otherwise false.
     */
    protected boolean isIdQuery(Query q)
    {
        boolean ok = false;
        if (q instanceof TermQuery)
        {
            TermQuery tq = (TermQuery) q;
            try
            {
                IdGeneratorIfc idGenerator = configuration.getIdGenerator();
                if (idGenerator.getFusionIdField().equals(tq.getFusionFieldName()))
                {
                    List<String> vals = tq.getFusionFieldValue();
                    if (vals != null && vals.size() == 1)
                    {
                        String fusionId = vals.get(0);
                        // perhaps fusionId consists of several fusion doc ids when the doc was merged
                        // but it is sufficient to check the first fusion doc only
                        String serverName = idGenerator.getSearchServerIdFromFusionId(fusionId);
                        idGenerator.getSearchServerDocIdFromFusionId(fusionId);
                        ok = configuration.getSearchServerConfigByName(serverName) != null;
                    }
                }
            }
            catch (Exception e)
            {
                // NOP
            }
        }
        return ok;
    }

    protected void processIdQuery(FusionRequest fusionRequest, FusionResponse fusionResponse)
    {
        log.debug("Detected id query: {}", fusionRequest.getQuery());
        // isIdQuery() ensures that the query is a TermQuery with one value and the id field
        TermQuery query = (TermQuery) fusionRequest.getParsedQuery();
        try
        {
            List<String> idVals = query.getTerm().getFusionFieldValue();
            String solrfusionMergedDocId = idVals.get(0);
            IdGeneratorIfc idGen = configuration.getIdGenerator();
            List<Throwable> collectedExceptions = new ArrayList<>();
            List<Document> collectedDocuments = new ArrayList<>();
            boolean isMoreLikeThisQuery = false;
            ResponseConsolidatorIfc consolidator = getNewResponseConsolidator();

            List<String> allServers = idGen.splitMergedId(solrfusionMergedDocId);
            for (String solrfusionDocId : allServers)
            {
                String searchServerName = idGen.getSearchServerIdFromFusionId(solrfusionDocId);
                SearchServerConfig searchServerConfig = configuration.getSearchServerConfigByName(searchServerName);
                String searchServerDocId = idGen.getSearchServerDocIdFromFusionId(solrfusionDocId);
                // set id for search server
                idVals.set(0, searchServerDocId);
                ScriptEnv env = getNewScriptEnv(fusionRequest.getLocale());
                configuration.getQueryMapper().mapQuery(configuration, searchServerConfig, query, env);
                XmlResponse result = sendAndReceive(true, fusionRequest, searchServerConfig);
                Exception se = result.getErrorReason();
                if (se == null)
                {
                    List<Document> matchDocuments = result.getMatchDocuments();
                    Document doc = null;
                    // response format of "more like this" query is different to normal search queries
                    // if "match" result is not empty, a "more like this" query has been processed
                    if (matchDocuments != null && matchDocuments.size() > 0)
                    {
                        doc = matchDocuments.get(0);
                        // the "response" result contains all similar documents
                        SearchServerResponseInfo similarInfo = new SearchServerResponseInfo(result.getNumFound(), null,
                            null, null);
                        ClosableIterator<Document, SearchServerResponseInfo> similarDocIt = new ClosableListIterator<>(
                            result.getDocuments(), similarInfo);
                        consolidator.addResultStream(searchServerConfig, similarDocIt, fusionRequest, null, null);
                        isMoreLikeThisQuery = true;
                    }
                    else
                    {
                        doc = result.getDocuments().get(0);
                    }
                    // map id, because completelyMapMergedDoc() call below depends on it
                    // the doc may not contain the id field, but doc merging needs it
                    String idFieldName = searchServerConfig.getIdFieldName();
                    if (doc.getSearchServerDocId(idFieldName) == null)
                    {
                        log.debug("Setting id={} in doc, because response of id query didn't contain a value.",
                            searchServerDocId);
                        doc.setSearchServerDocId(idFieldName, searchServerDocId);
                    }
                    configuration.getResponseMapper().mapResponse(configuration, searchServerConfig, doc, env,
                        Arrays.asList(idFieldName));
                    collectedDocuments.add(doc);
                }
                else
                {
                    collectedExceptions.add(se);
                }
                getNewResetQueryState().reset(query);
            }

            if (collectedDocuments.isEmpty())
            {
                fusionResponse.setResponseForException(collectedExceptions);
            }
            else
            {
                List<Document> mergedDocs = configuration.getResponseConsolidator(configuration).completelyMapMergedDoc(
                    collectedDocuments, null);
                ClosableIterator<Document, SearchServerResponseInfo> response = null;
                if (isMoreLikeThisQuery)
                {
                    // document of requested id goes to response "match"
                    response = consolidator.getResponseIterator(fusionRequest);
                    response.getExtraInfo().setAllMatchDocs(mergedDocs);
                }
                else
                {
                    SearchServerResponseInfo info = new SearchServerResponseInfo(1, null, null, null);
                    response = new ClosableListIterator<>(mergedDocs, info);
                }

                ResponseRendererIfc responseRenderer = configuration.getResponseRendererByType(
                    fusionRequest.getResponseType());
                // set state BEFORE response is rendered, because then the status is read out!
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
            log.error("Caught exception while processing id query {}", fusionRequest.getQuery(), e);
            fusionResponse.setResponseForException(e);
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
                // set state BEFORE response is rendered, because their the status is read out!
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
        ScriptEnv env = getNewScriptEnv(fusionRequest.getLocale());
        Query query = fusionRequest.getParsedQuery();
        List<Query> filterQuery = fusionRequest.getParsedFilterQuery();
        Query highlightQuery = fusionRequest.getParsedHighlightQuery();
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            log.info("Processing query for search server {}", searchServerConfig.getSearchServerName());
            if (mapQuery(env, searchServerConfig, Arrays.asList(query, highlightQuery)) &&
                mapQuery(env, searchServerConfig, filterQuery))
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
                        result.getHighlighting(), result.getFacetFields());
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

    protected ScriptEnv getNewScriptEnv(Locale locale)
    {
        ScriptEnv env = new ScriptEnv();
        env.setConfiguration(configuration);
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
                searchServerConfig, isIdQuery);
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
                if ("dismax".equals(queryType))
                {
                    adapter = newSolr1Adapter(adapter.getUrl());
                }
                InputStream is = adapter.sendQuery(searchServerParams, timeout,
                    searchServerConfig.getSearchServerVersion());
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
                    log.debug("Received from {}: {}", searchServerConfig.getSearchServerName(),
                        (result.getDocuments() != null) ? result.getDocuments().size() : -1);
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
        log.error("Caught exception while communicating with server {}", searchServerConfig.getSearchServerName(), e);
        XmlResponse responseError = new XmlResponse();
        responseError.setErrorReason(e);
        return responseError;
    }

    private XmlResponse handleSearchServerResponseException(SearchServerConfig searchServerConfig,
        SearchServerResponseException se)
    {
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

    protected boolean mapQuery(ScriptEnv env, SearchServerConfig searchServerConfig, List<Query> queryList)
    {
        boolean result = true;
        if (queryList != null)
        {
            for (Query query : queryList)
            {
                if (query != null)
                {
                    try
                    {
                        configuration.getQueryMapper().mapQuery(configuration, searchServerConfig, query, env);
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

    protected Query parseQuery(String query, Map<String, Float> boosts, Locale locale, FusionRequest fusionRequest)
    {
        Query queryObj = null;
        if (query != null)
        {
            try
            {
                QueryParserIfc queryParser = configuration.getQueryParser();
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