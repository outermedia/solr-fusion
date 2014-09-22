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
import org.outermedia.solrfusion.ParsedQuery;
import org.outermedia.solrfusion.configuration.PostProcessorStatus;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Searches for "empty" term filter query. If one is found the whole request is not passed to a solr server.
 * <p/>
 * Created by ballmann on 8/18/14.
 */
@Slf4j
public class FilterEmptyFq extends AbstractType
{
    protected List<String> excludedSolrFusionFieldNames;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        excludedSolrFusionFieldNames = parseFields(typeConfig, util, "//:ignore-fusion-name");
    }

    public static FilterEmptyFq getInstance()
    {
        return new FilterEmptyFq();
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = new TypeResult(Arrays.asList(PostProcessorStatus.CONTINUE.name()), null);
        List<ParsedQuery> filterQueries = (List<ParsedQuery>) env.getBinding(ScriptEnv.ENV_IN_MAPPED_FILTER_QUERIES);
        if (filterQueries != null)
        {
            for (ParsedQuery pq : filterQueries)
            {
                Query q = pq.getQuery();
                if (q instanceof TermQuery)
                {
                    TermQuery tq = (TermQuery) q;
                    String fusionFieldName = tq.getFusionFieldName();
                    if (!excludedSolrFusionFieldNames.contains(fusionFieldName) && tq.isSearchServerFieldEmpty())
                    {
                        result.getValues().set(0, PostProcessorStatus.DO_NOT_SEND_QUERY.name());
                        break;
                    }
                }
            }
        }
        return result;
    }
}
