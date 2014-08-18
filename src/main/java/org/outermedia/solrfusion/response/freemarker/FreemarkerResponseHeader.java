package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.FusionRequest;
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

    public FreemarkerResponseHeader(ClosableIterator<Document, SearchServerResponseInfo> docStream,
        FusionRequest request)
    {
        queryParams = new LinkedHashMap<>();
        multiValueQueryParams = new LinkedHashMap<>();

        int rows = 0;
        if (docStream != null)
        {
            rows = docStream.size();
        }
        queryParams.put(PAGE_SIZE.getRequestParamName(), String.valueOf(rows));
        String query = request.getQuery().getValue();
        if (query == null)
        {
            query = "";
        }
        queryParams.put(QUERY.getRequestParamName(), query);
        buildMultiValueParam(request.getFilterQuery(), FILTER_QUERY);
        addIfNotNull(SORT, request.getSolrFusionSortField());
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
