package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FieldMapping;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.query.parser.Query;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 04.06.14.
 */
@Setter
@Getter
@Slf4j
public class FusionRequest
{
    private String query;

    private String filterQuery;

    private int start;

    private int pageSize;

    private Locale locale;

    private Query parsedQuery;

    private Query parsedFilterQuery;

    private ResponseRendererType responseType;

    private String solrFusionSortField;

    private String searchServerSortField;

    private String fieldsToReturn;

    // otherwise desc
    private boolean sortAsc;


    public FusionRequest()
    {
        responseType = ResponseRendererType.XML;
    }

    public Map<String, Float> getBoosts()
    {
        return new HashMap<>(); // TODO from request params
    }

    public void setResponseTypeFromString(String responseTypeStr) throws ServletException
    {
        if (responseTypeStr != null)
        {
            String trimmedResponseTypeStr = responseTypeStr.trim().toUpperCase();
            try
            {
                responseType = ResponseRendererType.valueOf(trimmedResponseTypeStr);
            }
            catch (Exception e)
            {
                throw new ServletException("Found no renderer for given type '" + trimmedResponseTypeStr + "'", e);
            }
        }
    }

    public Map<String, String> buildSearchServerQueryParams(Configuration configuration,
        SearchServerConfig searchServerConfig) throws InvocationTargetException, IllegalAccessException
    {
        Map<String, String> searchServerParams = new HashMap<>();
        buildSearchServerQuery(parsedQuery, QUERY, configuration, searchServerConfig, searchServerParams);
        buildSearchServerQuery(parsedFilterQuery, FILTER_QUERY, configuration, searchServerConfig, searchServerParams);
        // get all documents from 0..min(MAXDOCS,start+page size)
        searchServerParams.put(START.getRequestParamName(), String.valueOf(0));
        int rows = Math.min(searchServerConfig.getMaxDocs(), getStart() + getPageSize());
        searchServerParams.put(PAGE_SIZE.getRequestParamName(), String.valueOf(rows));
        // TODO handle 1:n mapping i.e. 1 solrfusion field is mapped to several search server fields?
        // does it mean to sort by several search server fields?
        String searchServerSortField = mapFusionFieldToSearchServerField(getSolrFusionSortField(), configuration,
            searchServerConfig).iterator().next();
        setSearchServerSortField(searchServerSortField);
        searchServerParams.put(SORT.getRequestParamName(), searchServerSortField + (isSortAsc() ? " asc" : " desc"));
        String fieldsToReturn = mapFusionFieldListToSearchServerField(this.fieldsToReturn, configuration,
            searchServerConfig);
        if (!fieldsToReturn.isEmpty())
        {
            searchServerParams.put(FIELDS_TO_RETURN.getRequestParamName(), fieldsToReturn);
        }
        return searchServerParams;
    }

    /**
     * Map a list of fusion field names to a list of search server fields.
     *
     * @param fieldList separated by SPACE or comma or combinations of them
     * @param configuration
     * @param searchServerConfig
     * @return a string of field names separated by SPACE
     */
    protected String mapFusionFieldListToSearchServerField(String fieldList, Configuration configuration,
        SearchServerConfig searchServerConfig)
    {
        // preserve insertion order
        Set<String> fieldSet = new LinkedHashSet<>();
        if (fieldList != null)
        {
            // support " " and "," and combinations of them as separator
            StringTokenizer st = new StringTokenizer(fieldList, " ,");
            while (st.hasMoreTokens())
            {
                String fusionField = null;
                try
                {
                    fusionField = st.nextToken();
                    if (!fusionField.isEmpty())
                    {
                        fieldSet.addAll(
                            mapFusionFieldToSearchServerField(fusionField, configuration, searchServerConfig));
                    }
                }
                catch (Exception e)
                {
                    log.error("Caught exception while mapping query field {}. Ignoring field.", fusionField);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : fieldSet)
        {
            if (sb.length() > 0)
            {
                sb.append(" ");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    protected Set<String> mapFusionFieldToSearchServerField(String fusionField, Configuration configuration,
        SearchServerConfig searchServerConfig) throws InvocationTargetException, IllegalAccessException
    {
        // preserve insertion order
        Set<String> result = new LinkedHashSet<>();
        if (fusionField.equals("*"))
        {
            result.add("*");
        }
        else
        {
            List<FieldMapping> mappings = searchServerConfig.findAllMappingsForFusionField(fusionField);
            if (mappings.size() > 0)
            {
                for (FieldMapping fm : mappings)
                {
                    String searchServersName = fm.getSearchServersName();
                    if (searchServersName != null)
                    {
                        result.add(searchServersName);
                    }
                }
            }
            else
            {
                // special handling for score and id, because both are not mapped as the other fields
                if (ResponseMapperIfc.FUSION_FIELD_NAME_SCORE.equals(fusionField))
                {
                    result.add(ResponseMapperIfc.DOC_FIELD_NAME_SCORE);
                }
                String fusionIdField = configuration.getIdGenerator().getFusionIdField();
                if (fusionIdField.equals(fusionField))
                {
                    result.add(searchServerConfig.getIdFieldName());
                }
            }
            if (result.isEmpty())
            {
                result.add(ResponseMapperIfc.DOC_FIELD_NAME_SCORE);
                log.error(
                    "Can't handle fusion field '{}', because no mapping exist for search server {}. Using {} instead.",
                    fusionField, searchServerConfig.getSearchServerName(), result);
            }
        }
        return result;
    }

    protected void buildSearchServerQuery(Query query, SolrFusionRequestParams paramName, Configuration configuration,
        SearchServerConfig searchServerConfig, Map<String, String> searchServerParams)
        throws InvocationTargetException, IllegalAccessException
    {
        if (query != null)
        {
            QueryBuilderIfc queryBuilder = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            searchServerParams.put(paramName.getRequestParamName(),
                queryBuilder.buildQueryString(query, configuration));
        }
    }

}
