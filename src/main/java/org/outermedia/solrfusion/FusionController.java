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
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.QueryMapperIfc;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.query.QueryParserIfc;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 */
@Slf4j
public class FusionController implements FusionControllerIfc
{
    private Configuration configuration;
    private ResetQueryState queryResetter;
    private QueryMapperIfc queryMapper;
    private String queryStr;
    private String filterQueryStr;
    private Util util;
    private Throwable lastException;

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
        queryResetter = getNewResetQueryState();
        queryMapper = configuration.getQueryMapper();

        queryStr = fusionRequest.getQuery();
        filterQueryStr = fusionRequest.getFilterQuery();

        Map<String, Float> boosts = fusionRequest.getBoosts();
        Locale locale = fusionRequest.getLocale();
        Query query = parseQuery(fusionRequest.getQuery(), boosts, locale);
        Query filterQuery = parseQuery(filterQueryStr, boosts, locale);

        if (query == null)
        {
            fusionResponse.setResponseForQueryParseError(queryStr);
        }
        else if (filterQueryStr != null && filterQuery == null)
        {
            fusionResponse.setResponseForQueryParseError(filterQueryStr);
        }
        else
        {
            processQuery(configuration, fusionRequest, fusionResponse, query, filterQuery);
        }
    }

    protected void processQuery(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse,
        Query query, Query filterQuery)
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
                requestAllSearchServers(query, filterQuery, configuredSearchServers, consolidator);
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
                ClosableIterator<Document, SearchServerResponseInfo> response = consolidator.getResponseIterator();
                // TODO better to pass in a Writer in order to avoid building of very big String
                fusionResponse.setOkResponse(responseRenderer.getResponseString(response, queryStr, filterQueryStr));
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while processing search server's responses", e);
            fusionResponse.setResponseForException(e);
        }
    }

    protected void requestAllSearchServers(Query query, Query filterQuery,
        List<SearchServerConfig> configuredSearchServers, ResponseConsolidatorIfc consolidator)
    {
        ScriptEnv env = getNewScriptEnv();
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            if (mapQuery(query, env, searchServerConfig) && mapQuery(filterQuery, env, searchServerConfig))
            {
                sendAndReceive(query, filterQuery, consolidator, searchServerConfig);
            }
            queryResetter.reset(query);
            if (filterQuery != null)
            {
                queryResetter.reset(filterQuery);
            }
        }
    }

    protected ScriptEnv getNewScriptEnv()
    {
        return new ScriptEnv();
    }

    protected ResetQueryState getNewResetQueryState()
    {
        return new ResetQueryState();
    }

    protected void sendAndReceive(Query query, Query filterQuery, ResponseConsolidatorIfc consolidator,
        SearchServerConfig searchServerConfig)
    {
        try
        {
            Map<String, String> searchServerParams = new HashMap<>();

            String qParam = SolrFusionRequestParams.QUERY.getRequestParamName();
            buildSearchServerQuery(query, qParam, searchServerConfig, searchServerParams);

            String fqParam = SolrFusionRequestParams.FILTER_QUERY.getRequestParamName();
            buildSearchServerQuery(filterQuery, fqParam, searchServerConfig, searchServerParams);

            int timeout = configuration.getSearchServerConfigs().getTimeout();
            SearchServerAdapterIfc adapter = searchServerConfig.getInstance();
            InputStream is = adapter.sendQuery(searchServerParams, timeout);
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            XmlResponse result = responseParser.parse(is);
            SearchServerResponseInfo info = new SearchServerResponseInfo(result.getNumFound());
            ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
                result.getDocuments(), info);
            consolidator.addResultStream(configuration, searchServerConfig, docIterator);
        }
        catch (SearchServerResponseException se)
        {
            try
            {
                ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                    configuration.getDefaultResponseParser());
                XmlResponse responseError = responseParser.parse(se.getHttpContent());
                if(responseError != null)
                {
                    se.setResponseError(responseError.getResponseErrors());
                }
            }
            catch (Exception e)
            {
                // depending on solr's version, a well formed error message is provided or not
                log.warn("Couldn't parse error response", e);
            }
            consolidator.addErrorResponse(se);
        }
        catch (Exception e)
        {
            log.error("Caught exception while communicating with server {}", searchServerConfig.getSearchServerName(),
                e);
        }
    }

    protected void buildSearchServerQuery(Query query, String qParam, SearchServerConfig searchServerConfig,
        Map<String, String> searchServerParams) throws InvocationTargetException, IllegalAccessException
    {
        if (query != null)
        {
            QueryBuilderIfc queryBuilder = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            searchServerParams.put(qParam, queryBuilder.buildQueryString(query, configuration));
        }
    }

    protected boolean mapQuery(Query query, ScriptEnv env, SearchServerConfig searchServerConfig)
    {
        boolean result = true;
        if (query != null)
        {
            try
            {
                queryMapper.mapQuery(configuration, searchServerConfig, query, env);
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

    protected Query parseQuery(String query, Map<String, Float> boosts, Locale locale)
    {
        Query queryObj = null;
        if (query != null)
        {
            try
            {
                QueryParserIfc queryParser = configuration.getQueryParser();
                try
                {
                    queryObj = queryParser.parse(configuration, boosts, query, locale);
                }
                catch (Exception e)
                {
                    log.error("Parsing of queryStr {} failed.", query, e);
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while parsing queryStr: {}", query, e);
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