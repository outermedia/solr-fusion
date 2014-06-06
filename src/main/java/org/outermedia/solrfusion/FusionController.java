package org.outermedia.solrfusion;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.QueryMapper;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 */
@Slf4j
public class FusionController
{
    private Configuration configuration;
    private ResetQueryState queryResetter;
    private QueryMapper queryMapper;

    public FusionController(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void process(FusionRequest fusionRequest, FusionResponse fusionResponse)
    {
        Map<String, Float> boosts = fusionRequest.getBoosts();
        queryResetter = new ResetQueryState();
        queryMapper = new QueryMapper();
        Query query = parseQuery(fusionResponse, boosts, fusionRequest.getQuery());
        ScriptEnv env = new ScriptEnv();

        if (query != null)
        {
            List<SearchServerConfig> configuredSearchServers = configuration.getSearchServerConfigs().getSearchServerConfigs();
            if (configuredSearchServers.isEmpty())
            {
                fusionResponse.setResponseForNoSearchServerConfiguredError();
            }
            else
            {
                ResponseConsolidatorIfc consolidator = configuration.getResponseConsolidatorFactory().getImplementation();
                for (SearchServerConfig searchServerConfig : configuredSearchServers)
                {
                    if (mapQuery(query, env, searchServerConfig))
                    {
                        sendAndReceive(consolidator, searchServerConfig);
                    }
                }
                if (consolidator.numberOfResponseStreams() < configuration.getDisasterLimit())
                {
                    fusionResponse.setResponseForTooLessServerAnsweredError(configuration.getDisasterMessage());
                    consolidator.reset();
                }
                else
                {
                    ClosableIterator<Document> response = consolidator.getResponseStream();
                    // TODO get response renderer and attach it to fusionResponse?
                }
            }
        }
    }

    private void sendAndReceive(ResponseConsolidatorIfc consolidator, SearchServerConfig searchServerConfig)
    {
        SearchServerAdapterIfc adapter = searchServerConfig.getImplementation();
        String searchServerQueryStr = ""; // TODO = query.createSearchServerQueryString();
        try
        {
            ClosableIterator<Document> docIterator = adapter.sendQuery(searchServerQueryStr);
            if (docIterator != null)
            {
                consolidator.addResultStream(docIterator);
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while communicating with server {}", searchServerConfig.getSearchServerName(), e);
        }
    }

    private boolean mapQuery(Query query, ScriptEnv env, SearchServerConfig searchServerConfig)
    {
        boolean result = true;
        try
        {
            queryMapper.mapQuery(searchServerConfig, query, env);
        }
        catch (Exception e)
        {
            log.error("Caught exception while mapping fusion query to server {}", searchServerConfig.getSearchServerName(), e);
            result = false;
        }
        finally
        {
            queryResetter.reset(query);
        }
        return result;
    }

    protected Query parseQuery(FusionResponse fusionResponse, Map<String, Float> boosts, String query)
    {
        QueryParserIfc queryParser = configuration.getQueryParser();
        Query queryObj = null;
        try
        {
            queryObj = queryParser.parse(configuration, boosts, query);
        }
        catch (Exception e)
        {
            log.error("Parsing of query {} failed.", query, e);
        }
        if (queryObj == null)
        {
            fusionResponse.setResponseForQueryParseError();
        }
        return queryObj;
    }
}
