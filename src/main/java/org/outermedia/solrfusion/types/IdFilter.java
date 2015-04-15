package org.outermedia.solrfusion.types;

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
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Remove the id query term from a query if the id references no document of the currently used Solr server.
 * <p/>
 * Created by ballmann on 8/18/14.
 */
@Slf4j
public class IdFilter extends AbstractType
{
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        // NOP
    }

    public static IdFilter getInstance()
    {
        return new IdFilter();
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env, ConversionDirection dir)
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
                    List<String> allSearchServerNames = env.getConfiguration().allSearchServerNames();
                    for (String singleFusionId : mergedIds)
                    {
                        // if the current search server is not the originator, remove the id term query
                        String sourceSearchServer = idGen.getSearchServerIdFromFusionId(singleFusionId);
                        if (sourceSearchServer.equals(searchServerConfig.getSearchServerName()))
                        {
                            result = new TypeResult(Arrays.asList(
                                idGen.getSearchServerDocIdFromFusionId(mergedFusionId, allSearchServerNames)), null,
                                isReturnsFullQueries());
                            break;
                        }
                    }
                    if (result == null)
                    {
                        // remove value
                        result = new TypeResult(null, null, false);
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
                result = new TypeResult(values, null, isReturnsFullQueries());
            }
        }
        return result;
    }
}
