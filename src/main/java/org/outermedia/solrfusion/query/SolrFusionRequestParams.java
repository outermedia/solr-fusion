package org.outermedia.solrfusion.query;

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

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class enumerates all supported HTTP request parameters.
 *
 * Created by ballmann on 7/4/14.
 */
@Getter
public enum SolrFusionRequestParams
{
    QUERY("q", false, null),
    WRITER_TYPE("wt", false, null),
    FILTER_QUERY("fq", false, null),
    START("start", false, null),
    PAGE_SIZE("rows", false, null),
    SORT("sort", false, null),
    FIELDS_TO_RETURN("fl", false, null),
    HIGHLIGHT_FIELDS_TO_RETURN("hl.fl", false, null),
    HIGHLIGHT_PRE("hl.simple.pre", false, null),
    HIGHLIGHT_POST("hl.simple.post", false, null),
    HIGHLIGHT_QUERY("hl.q", false, null),
    HIGHLIGHT("hl", false, null),
    FACET("facet", false, null),
    FACET_MINCOUNT("facet.mincount", false, null),
    FACET_LIMIT("facet.limit", false, null),
    FACET_SORT_FIELD("f.([^.]+).facet.sort", true, "f.%s.facet.sort"),
    FACET_SORT("facet.sort", false, null),
    FACET_PREFIX("facet.prefix", false, null),
    FACET_FIELD("facet.field", false, null),
    QUERY_TYPE("qt", false, null),
    QUERY_FIELD("qf", false, null),
    MINIMUM_MATCH("mm", false, null);

    protected String requestParamName;

    @Getter(AccessLevel.NONE)
    protected Pattern requestPattern;

    protected String searchServerParamPattern;

    SolrFusionRequestParams(String n, boolean isPattern, String searchServerParamPattern)
    {
        requestParamName = n;
        this.searchServerParamPattern = searchServerParamPattern;
        if (isPattern)
        {
            requestPattern = Pattern.compile(n);
        }
    }

    public boolean isPattern()
    {
        return requestPattern != null;
    }

    public String matches(String givenRequestParamName)
    {
        String result = null;
        if (requestPattern != null)
        {
            Matcher m = requestPattern.matcher(givenRequestParamName);
            if (m.find())
            {
                result = givenRequestParamName;
                if (m.groupCount() > 0)
                {
                    result = m.group(1);
                }
            }
        }
        else
        {
            if (requestParamName.equals(givenRequestParamName))
            {
                result = requestParamName;
            }
        }
        return result;
    }

    public String buildFusionFacetSortFieldParam(String fusionField, Locale locale)
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, locale);
        formatter.format(searchServerParamPattern, fusionField);
        return sb.toString();
    }
}
