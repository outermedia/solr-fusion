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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Flatten multiple values of one field to one value which is necessary when the destination field is a single value.
 *
 * Created by ballmann on 7/16/14.
 */
@ToString(callSuper = true)
@Getter
@Setter
@Slf4j
public class MultiValueMerger extends AbstractType
{
    private String range;

    private String separator;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        range = getConfigString("range", typeConfig, util);
        separator = getConfigString("separator", typeConfig, util, false);
        logBadConfiguration(range != null && separator != null, typeConfig);
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        List<String> newValues = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int limit = values.size();
        if (!"all".equals(range))
        {
            try
            {
                limit = Integer.parseInt(range);
            }
            catch (Exception e)
            {
                log.warn("Can't parse number '{}'. Using 'all' instead.", range);
            }
        }
        for (int i = 0; i < limit && i < values.size(); i++)
        {
            String s = values.get(i);
            if (s != null)
            {
                if (i > 0)
                {
                    sb.append(separator);
                }
                sb.append(s);
            }
        }
        if (sb.length() > 0)
        {
            newValues.add(sb.toString());
        }
        if (!newValues.isEmpty())
        {
            if (newValues.size() != values.size() && facetDocCounts != null && facetDocCounts.size() > 0)
            {
                log.error("Script type MultiValueMerger merged values of search server field {} although facet word " +
                        "counts are present. Using original facet doc count values.",
                    env.getBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_FIELD));
            }
            result = new TypeResult(newValues, facetDocCounts);
        }
        return result;
    }

    public static MultiValueMerger getInstance()
    {
        return new MultiValueMerger();
    }
}
