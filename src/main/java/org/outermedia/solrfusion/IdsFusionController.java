package org.outermedia.solrfusion;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.CollectingResponseConsolidator;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ballmann on 4/14/15.
 */

@Slf4j
public class IdsFusionController extends FusionController
{
    protected Multimap<String> idsPerServer = new Multimap<>();

    @Override
    public void process(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse)
        throws InvocationTargetException, IllegalAccessException
    {
        this.configuration = configuration;

        // collect all ids which belong to the same server
        // only here it is possible to collect all ids of a specific solr server
        SolrFusionRequestParam idsQuery = fusionRequest.getIds();
        StringTokenizer st = new StringTokenizer(idsQuery.getValue(), ", ");
        IdGeneratorIfc idGen = configuration.getIdGenerator();
        while (st.hasMoreTokens())
        {
            String fusionDocId = st.nextToken();
            List<String> allSearchServerNames = configuration.allSearchServerNames();
            if (idGen.isMergedDocument(fusionDocId, allSearchServerNames))
            {
                List<String> all = idGen.splitMergedId(fusionDocId);
                for (String fId : all)
                {
                    String searchServer = idGen.getSearchServerIdFromFusionId(fId);
                    String solrDocId = idGen.getSearchServerDocIdFromFusionId(fId, allSearchServerNames);
                    idsPerServer.put(searchServer, solrDocId);
                }
            }
            else
            {
                String searchServer = idGen.getSearchServerIdFromFusionId(fusionDocId);
                String solrDocId = idGen.getSearchServerDocIdFromFusionId(fusionDocId, allSearchServerNames);
                idsPerServer.put(searchServer, solrDocId);
            }
        }
        log.info("Split ids into: "+ idsPerServer);

        processRequestWithQParam(configuration, fusionRequest, fusionResponse);
    }

    /**
     * Ignore the configured disaster limit.
     *
     * @param configuration
     * @return
     */
    @Override
    protected int getDisasterLimit(Configuration configuration)
    {
        return 0;
    }

    /**
     * We have to merge the ids manually, so we can't use the default consolidator.
     *
     * @return
     */
    @Override
    protected ResponseConsolidatorIfc getNewResponseConsolidator()
    {
        ResponseConsolidatorIfc result = new CollectingResponseConsolidator();
        try
        {
            result.initConsolidator(configuration);
        }
        catch (Exception e)
        {
            log.error("Caught exception while initializing CollectingResponseConsolidator", e);
        }
        return result;
    }

    /**
     * Prepare requests with the ids per solr server.
     *
     * @param fusionRequest
     * @param configuredSearchServers
     * @param consolidator
     */
    @Override
    protected void requestAllSearchServers(FusionRequest fusionRequest,
        List<SearchServerConfig> configuredSearchServers, ResponseConsolidatorIfc consolidator)
    {
        ScriptEnv env = getNewScriptEnv(fusionRequest);
        Query query = fusionRequest.getParsedQuery();
        List<ParsedQuery> filterQuery = fusionRequest.getParsedFilterQuery();
        Query highlightQuery = fusionRequest.getParsedHighlightQuery();
        List<SolrFusionUriBuilderIfc> serverRequests = new ArrayList<>();
        // we always want xml back
        ResponseRendererType origResponseType = fusionRequest.getResponseType();
        fusionRequest.setResponseType(ResponseRendererType.XML);
        SolrFusionRequestParam origIds = fusionRequest.getIds();

        // at first create all solr requests
        // because the query objects store the mapped values, it is not possible to map the queries in parallel
        for (SearchServerConfig searchServerConfig : configuredSearchServers)
        {
            Collection<String> solrIds = idsPerServer.get(searchServerConfig.getSearchServerName());
            if (solrIds != null && solrIds.size() > 0)
            {
                log.info("Processing query for search server {}", searchServerConfig.getSearchServerName());

                // rewrite ids which affect the current solr server
                StringBuilder filteredIds = new StringBuilder();
                for (String s : solrIds)
                {
                    if (filteredIds.length() > 0)
                    {
                        filteredIds.append(",");
                    }
                    filteredIds.append(s);
                }
                fusionRequest.setIds(new SolrFusionRequestParam(filteredIds.toString(), null, null));

                List<ParsedQuery> queryList = Arrays.asList(
                    new ParsedQuery(fusionRequest.getQuery().getValue(), query));
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

        // reset original response type and ids
        fusionRequest.setResponseType(origResponseType);
        fusionRequest.setIds(origIds);
    }

    public static class Factory
    {
        public static FusionControllerIfc getInstance()
        {
            return new IdsFusionController();
        }
    }
}
