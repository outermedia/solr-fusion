package org.outermedia.solrfusion;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ControllerFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.*;
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
    private FusionController()
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

        String queryStr = fusionRequest.getQuery();
        String filterQueryStr = fusionRequest.getFilterQuery();

        Map<String, Float> boosts = fusionRequest.getBoosts();
        Locale locale = fusionRequest.getLocale();
        fusionRequest.setParsedQuery(parseQuery(queryStr, boosts, locale, fusionRequest));
        fusionRequest.setParsedFilterQuery(parseQuery(filterQueryStr, boosts, locale, fusionRequest));

        if (fusionRequest.getParsedQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(queryStr, fusionRequest.buildErrorMessage());
        }
        else if (filterQueryStr != null && filterQueryStr.trim().length() > 0 &&
            fusionRequest.getParsedFilterQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(filterQueryStr, fusionRequest.buildErrorMessage());
        }
        else
        {
            processQuery(configuration, fusionRequest, fusionResponse);
        }
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
                XmlResponse result = sendAndReceive(fusionRequest, searchServerConfig);
                Exception se = result.getErrorReason();
                if (se == null)
                {
                    Document doc = result.getDocuments().get(0);
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
                Document mergedDoc = configuration.getResponseConsolidator().completelyMapMergedDoc(configuration,
                    idGen.getFusionIdField(), collectedDocuments);
                SearchServerResponseInfo info = new SearchServerResponseInfo(1);
                ClosableIterator<Document, SearchServerResponseInfo> response = new ClosableListIterator<>(
                    Arrays.asList(mergedDoc), info);
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
            return configuration.getResponseConsolidator();
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
                    configuration, fusionRequest);
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
        log.debug("Requesting all configured servers for query: {}", fusionRequest.getQuery());
        ScriptEnv env = getNewScriptEnv(fusionRequest.getLocale());
        Query query = fusionRequest.getParsedQuery();
        Query filterQuery = fusionRequest.getParsedFilterQuery();
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            if (mapQuery(query, env, searchServerConfig) && mapQuery(filterQuery, env, searchServerConfig))
            {
                XmlResponse result = sendAndReceive(fusionRequest, searchServerConfig);
                Exception se = result.getErrorReason();
                if (se == null)
                {
                    SearchServerResponseInfo info = new SearchServerResponseInfo(result.getNumFound());
                    ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
                        result.getDocuments(), info);

                    consolidator.addResultStream(configuration, searchServerConfig, docIterator, fusionRequest);
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

    protected XmlResponse sendAndReceive(FusionRequest fusionRequest, SearchServerConfig searchServerConfig)
    {
        try
        {
            Map<String, String> searchServerParams = fusionRequest.buildSearchServerQueryParams(configuration,
                searchServerConfig);
            int timeout = configuration.getSearchServerConfigs().getTimeout();
            SearchServerAdapterIfc adapter = searchServerConfig.getInstance();
            InputStream is = adapter.sendQuery(searchServerParams, timeout);
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            XmlResponse result = responseParser.parse(is);
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
            log.trace("Received from {}: {}", searchServerConfig.getSearchServerName(), result.toString());
            return result;
        }
        catch (SearchServerResponseException se)
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
        catch (Exception e)
        {
            log.error("Caught exception while communicating with server {}", searchServerConfig.getSearchServerName(),
                e);
            XmlResponse responseError = new XmlResponse();
            responseError.setErrorReason(e);
            return responseError;
        }
    }

    protected boolean mapQuery(Query query, ScriptEnv env, SearchServerConfig searchServerConfig)
    {
        boolean result = true;
        if (query != null)
        {
            try
            {
                configuration.getQueryMapper().mapQuery(configuration, searchServerConfig, query, env);
            }
            catch (Exception e)
            {
                log.error("Caught exception while mapping fusion queryStr to server {}",
                    searchServerConfig.getSearchServerName(), e);
                result = false;
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