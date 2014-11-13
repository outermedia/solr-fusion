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
 * Searches for a specified filter query which is a simple term query. If it is found and the searched value is equal to
 * the specified value, then the whole request is passed to a solr server. Otherwise not.
 * <p/>
 * Created by ballmann on 8/18/14.
 */
@Slf4j
public class FilterSpecificFq extends AbstractType
{
    protected String solrFusionFieldName;
    protected String solrFusionFieldValue;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        try
        {
            solrFusionFieldName = util.getValueOfXpath("//:fusion-name", typeConfig, true);
            solrFusionFieldValue = util.getValueOfXpath("//:fusion-value", typeConfig, true);
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: " + elementListToString(typeConfig), e);
        }
    }

    public static FilterSpecificFq getInstance()
    {
        return new FilterSpecificFq();
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = new TypeResult(Arrays.asList(PostProcessorStatus.CONTINUE.name()), null,
            isReturnsFullQueries());
        List<ParsedQuery> filterQueries = (List<ParsedQuery>) env.getBinding(ScriptEnv.ENV_IN_MAPPED_FILTER_QUERIES);
        if (filterQueries != null)
        {
            for (ParsedQuery pq : filterQueries)
            {
                Query q = pq.getQuery();
                if (q instanceof TermQuery)
                {
                    TermQuery tq = (TermQuery) q;
                    if (solrFusionFieldName.equals(tq.getFusionFieldName()))
                    {
                        if (!solrFusionFieldValue.equals(tq.getFirstFusionFieldValue()))
                        {
                            result.getValues().set(0, PostProcessorStatus.DO_NOT_SEND_QUERY.name());
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }
}
