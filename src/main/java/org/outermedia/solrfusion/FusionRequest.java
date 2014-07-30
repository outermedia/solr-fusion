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

    private String highlightingFieldsToReturn;

    // otherwise desc
    private boolean sortAsc;

    private List<String> errors;

    public FusionRequest()
    {
        responseType = ResponseRendererType.JSON;
        errors = new ArrayList<>();
    }

    public Map<String, Float> getBoosts()
    {
        return new HashMap<>(); // TODO from request params
    }

    public void setResponseTypeFromString(String responseTypeStr, FusionRequest fusionRequest)
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
                fusionRequest.addError(
                    "Found no renderer for given type '" + trimmedResponseTypeStr + "'. Cause: " + e.getMessage());
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
        Set<String> searchServerFieldSet = mapFusionFieldToSearchServerField(getSolrFusionSortField(), configuration,
            searchServerConfig);
        if (searchServerFieldSet.isEmpty())
        {
            log.error("Found not mapping for sort field '{}'", getSolrFusionSortField());
        }
        String searchServerSortField = searchServerFieldSet.iterator().next();
        if (searchServerFieldSet.size() > 1)
        {
            log.error("Found ambiguous mapping for sort field '{}'. Using: {}", getSolrFusionSortField(),
                searchServerSortField);
        }
        setSearchServerSortField(searchServerSortField);
        searchServerParams.put(SORT.getRequestParamName(), searchServerSortField + (isSortAsc() ? " asc" : " desc"));
        String fusionFieldsToReturn = fieldsToReturn;
        if (fusionFieldsToReturn == null)
        {
            fusionFieldsToReturn = "*";
        }
        fusionFieldsToReturn += " " + getSolrFusionSortField();
        // TODO still necessary when highlighting is supported?
        if (highlightingFieldsToReturn != null)
        {
            fusionFieldsToReturn += " " + highlightingFieldsToReturn;
        }
        String fieldsToReturn = mapFusionFieldListToSearchServerField(fusionFieldsToReturn, configuration,
            searchServerConfig);
        searchServerParams.put(FIELDS_TO_RETURN.getRequestParamName(), fieldsToReturn);
        return searchServerParams;
    }

    /**
     * Map a list of fusion field names to a list of search server fields.
     *
     * @param fieldList          separated by SPACE or comma or combinations of them
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

    public Set<String> mapFusionFieldToSearchServerField(String fusionField, Configuration configuration,
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
                queryBuilder.buildQueryString(query, configuration, searchServerConfig, locale));
        }
    }

    public void addError(String msg)
    {
        errors.add(msg);
    }

    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public String buildErrorMessage()
    {
        StringBuilder sb = new StringBuilder();
        for (String s : errors)
        {
            sb.append("ERROR: ");
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }

    public void addError(String msg, Exception e)
    {
        if (e != null)
        {
            String cause = collectCauses(e);
            if (cause.length() > 0)
            {
                msg += "\n" + cause;
            }
        }
        addError(msg);
    }

    protected String collectCauses(Throwable e)
    {
        StringBuilder sb = new StringBuilder();
        while (e != null)
        {
            sb.append(e.getMessage());
            sb.append("\n");
            e = e.getCause();
        }
        return sb.toString();
    }
}
