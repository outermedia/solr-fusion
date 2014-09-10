package org.outermedia.solrfusion.types;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Remove the id query term from a query if the id references no document of the currently used Solr server.
 *
 * Created by ballmann on 8/18/14.
 */
@Slf4j
public class IdFilter extends AbstractType
{
    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        // NOP
    }

    public static IdFilter getInstance()
    {
        return new IdFilter();
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        if (values.size() > 0)
        {
            if (dir == ConversionDirection.FUSION_TO_SEARCH)
            {
                SearchServerConfig searchServerConfig = env.getSearchServerConfig();
                String mergedFusionId = values.get(0);
                try
                {
                    IdGeneratorIfc idGen = env.getConfiguration().getIdGenerator();
                    List<String> mergedIds = idGen.splitMergedId(mergedFusionId);
                    for (String singleFusionId : mergedIds)
                    {
                        // if the current search server is not the originator, remove the id term query
                        String sourceSearchServer = idGen.getSearchServerIdFromFusionId(singleFusionId);
                        if (sourceSearchServer.equals(searchServerConfig.getSearchServerName()))
                        {
                            result = new TypeResult(
                                Arrays.asList(idGen.getSearchServerDocIdFromFusionId(mergedFusionId)), null);
                            break;
                        }
                    }
                    if (result == null)
                    {
                        // remove value
                        result = new TypeResult(null, null);
                    }
                }
                catch (Exception e)
                {
                    log.error("Caught error while creating id generator.", e);
                }
            }
            else
            {
                // search server id to fusion id: prefix will be added later
                result = new TypeResult(values, null);
            }
        }
        return result;
    }
}
