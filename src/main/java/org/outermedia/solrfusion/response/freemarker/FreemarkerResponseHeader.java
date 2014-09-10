package org.outermedia.solrfusion.response.freemarker;

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
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Data holder class to represent a response header in the freemarker template.
 *
 * @author stephan
 */
@Getter
public class FreemarkerResponseHeader
{
    private Map<String, String> queryParams;
    private Map<String, List<String>> multiValueQueryParams;

    private long queryTime;

    public FreemarkerResponseHeader(ClosableIterator<Document, SearchServerResponseInfo> docStream,
        FusionRequest request, FusionResponse fusionResponse)
    {
        queryParams = new LinkedHashMap<>();
        multiValueQueryParams = new LinkedHashMap<>();

        queryTime = fusionResponse.getQueryTime();

        int rows = 0;
        if (docStream != null)
        {
            rows = docStream.size();
        }
        queryParams.put(PAGE_SIZE.getRequestParamName(), String.valueOf(rows));
        if (request.getStart().isContainedInRequest())
        {
            queryParams.put(START.getRequestParamName(), request.getStart().getValue());
        }
        String query = request.getQuery().getValue();
        if (query == null)
        {
            query = "";
        }
        queryParams.put(QUERY.getRequestParamName(), query);
        buildMultiValueParam(request.getFilterQuery(), FILTER_QUERY);
        if (request.getSort().isContainedInRequest())
        {
            addIfNotNull(SORT, request.getSort().getValue());
        }
        addIfNotNull(FIELDS_TO_RETURN, request.getFieldsToReturn().getValue());
        addIfNotNull(QUERY_TYPE, request.getQueryType().getValue());

        // highlights
        addIfNotNull(SolrFusionRequestParams.HIGHLIGHT, request.getHighlight().getValue());
        addIfNotNull(HIGHLIGHT_PRE, request.getHighlightPre().getValue());
        addIfNotNull(HIGHLIGHT_POST, request.getHighlightPost().getValue());
        addIfNotNull(HIGHLIGHT_FIELDS_TO_RETURN, request.getHighlightingFieldsToReturn().getValue());
        addIfNotNull(HIGHLIGHT_QUERY, request.getHighlightQuery().getValue());

        // facets
        addIfNotNull(FACET, request.getFacet().getValue());
        addIfNotNull(FACET_LIMIT, request.getFacetLimit().getValue());
        addIfNotNull(FACET_MINCOUNT, request.getFacetMincount().getValue());
        addIfNotNull(FACET_PREFIX, request.getFacetPrefix().getValue());
        addIfNotNull(FACET_SORT, request.getFacetSort().getValue());
        buildMultiValueParam(request.getFacetFields(), FACET_FIELD);
        List<SolrFusionRequestParam> facetSortFields = request.getFacetSortFields();
        if (facetSortFields != null)
        {
            for (SolrFusionRequestParam sp : facetSortFields)
            {
                String fusionParam = SolrFusionRequestParams.FACET_SORT_FIELD.buildFusionFacetSortFieldParam(
                    sp.getParamNameVariablePart(), request.getLocale());
                queryParams.put(fusionParam, sp.getValue());
            }
        }
    }

    protected void buildMultiValueParam(List<SolrFusionRequestParam> facetFieldParams,
        SolrFusionRequestParams fusionParam)
    {
        List<String> paramValues = new ArrayList<>();
        if (facetFieldParams != null)
        {
            for (SolrFusionRequestParam sp : facetFieldParams)
            {
                paramValues.add(sp.getValue());
            }
            if (!paramValues.isEmpty())
            {
                multiValueQueryParams.put(fusionParam.getRequestParamName(), paramValues);
            }
        }
    }

    protected void addIfNotNull(SolrFusionRequestParams paramName, String paramValue)
    {
        if (paramValue != null)
        {
            queryParams.put(paramName.getRequestParamName(), paramValue);
        }
    }
}
