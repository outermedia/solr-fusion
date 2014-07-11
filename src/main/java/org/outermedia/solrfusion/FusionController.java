package org.outermedia.solrfusion;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
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

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 04.06.14.
 */
@Slf4j
public class FusionController implements FusionControllerIfc
{
    private Configuration configuration;
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

        String queryStr = fusionRequest.getQuery();
        String filterQueryStr = fusionRequest.getFilterQuery();

        Map<String, Float> boosts = fusionRequest.getBoosts();
        Locale locale = fusionRequest.getLocale();
        fusionRequest.setParsedQuery(parseQuery(queryStr, boosts, locale));
        fusionRequest.setParsedFilterQuery(parseQuery(filterQueryStr, boosts, locale));

        if (fusionRequest.getParsedQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(queryStr);
        }
        else if (filterQueryStr != null && fusionRequest.getParsedFilterQuery() == null)
        {
            fusionResponse.setResponseForQueryParseError(filterQueryStr);
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
                // TODO better to pass in a Writer in order to avoid building of very big String
                String responseString = responseRenderer.getResponseString(configuration, response, fusionRequest.getQuery(),
                    fusionRequest.getFilterQuery());
                fusionResponse.setOkResponse(responseString);
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
        ScriptEnv env = getNewScriptEnv();
        Query query = fusionRequest.getParsedQuery();
        Query filterQuery = fusionRequest.getParsedFilterQuery();
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            if (mapQuery(query, env, searchServerConfig) && mapQuery(filterQuery, env, searchServerConfig))
            {
                sendAndReceive(fusionRequest, consolidator, searchServerConfig);
            }
            getNewResetQueryState().reset(query);
            if (filterQuery != null)
            {
                getNewResetQueryState().reset(filterQuery);
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

    protected void sendAndReceive(FusionRequest fusionRequest, ResponseConsolidatorIfc consolidator,
        SearchServerConfig searchServerConfig)
    {
        try
        {
            Map<String, String> searchServerParams = buildQueryParams(fusionRequest, searchServerConfig);
            int timeout = configuration.getSearchServerConfigs().getTimeout();
            SearchServerAdapterIfc adapter = searchServerConfig.getInstance();
            InputStream is = adapter.sendQuery(searchServerParams, timeout);
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            XmlResponse result = responseParser.parse(is);
            SearchServerResponseInfo info = new SearchServerResponseInfo(result.getNumFound());
            ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
                result.getDocuments(), info);
            consolidator.addResultStream(configuration, searchServerConfig, docIterator, fusionRequest);
        }
        catch (SearchServerResponseException se)
        {
            try
            {
                ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                    configuration.getDefaultResponseParser());
                XmlResponse responseError = responseParser.parse(se.getHttpContent());
                if (responseError != null)
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

    protected Map<String, String> buildQueryParams(FusionRequest fusionRequest, SearchServerConfig searchServerConfig)
        throws InvocationTargetException, IllegalAccessException
    {
        Query query = fusionRequest.getParsedQuery();
        Query filterQuery = fusionRequest.getParsedFilterQuery();
        Map<String, String> searchServerParams = new HashMap<>();
        buildSearchServerQuery(query, QUERY, searchServerConfig, searchServerParams);
        buildSearchServerQuery(filterQuery, FILTER_QUERY, searchServerConfig, searchServerParams);
        // get all documents from 0..min(MAXDOCS,start+page size)
        searchServerParams.put(START.getRequestParamName(), String.valueOf(0));
        int rows = Math.min(searchServerConfig.getMaxDocs(), fusionRequest.getStart() + fusionRequest.getPageSize());
        searchServerParams.put(PAGE_SIZE.getRequestParamName(), String.valueOf(rows));
        String searchServerSortField = mapFusionFieldToSearchServerField(fusionRequest.getSolrFusionSortField(),
            searchServerConfig);
        fusionRequest.setSearchServerSortField(searchServerSortField);
        searchServerParams.put(SORT.getRequestParamName(),
            searchServerSortField + (fusionRequest.isSortAsc() ? " asc" : " desc"));
        return searchServerParams;
    }

    protected String mapFusionFieldToSearchServerField(String fusionField, SearchServerConfig searchServerConfig)
        throws InvocationTargetException, IllegalAccessException
    {
        List<FieldMapping> mappings = searchServerConfig.findAllMappingsForFusionField(fusionField);
        String result = null;
        // TODO handle 1:n mapping i.e. 1 solrfusion field is mapped to several search server fields?
        // does it mean to sort by several search server fields?
        if (mappings.size() > 0)
        {
            result = mappings.get(0).getSearchServersName();
        }
        else
        {
            if (ResponseMapperIfc.FUSION_FIELD_NAME_SCORE.equals(fusionField))
            {
                result = ResponseMapperIfc.DOC_FIELD_NAME_SCORE;
            }
            String fusionIdField = configuration.getIdGenerator().getFusionIdField();
            if (fusionIdField.equals(fusionField))
            {
                result = searchServerConfig.getIdFieldName();
            }
        }
        if (result == null)
        {
            result = ResponseMapperIfc.DOC_FIELD_NAME_SCORE;
            log.error(
                "Can't sort by fusion field '{}', because no mapping exist for search server {}. Using {} instead.",
                fusionField, searchServerConfig.getSearchServerName(), result);
        }
        return result;
    }

    protected void buildSearchServerQuery(Query query, SolrFusionRequestParams paramName,
        SearchServerConfig searchServerConfig, Map<String, String> searchServerParams)
        throws InvocationTargetException, IllegalAccessException
    {
        if (query != null)
        {
            QueryBuilderIfc queryBuilder = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            searchServerParams.put(paramName.getRequestParamName(),
                queryBuilder.buildQueryString(query, configuration));
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