package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.query.parser.MetaInfo;
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
    private SolrFusionRequestParam query;
    private List<SolrFusionRequestParam> filterQuery;
    private SolrFusionRequestParam start;
    private SolrFusionRequestParam pageSize;
    private Locale locale;
    private Query parsedQuery;
    private List<Query> parsedFilterQuery;
    private ResponseRendererType responseType;
    private SolrFusionRequestParam sort;
    private SolrFusionRequestParam fieldsToReturn;
    private SolrFusionRequestParam queryType;
    private SolrFusionRequestParam queryFields;

    private SolrFusionRequestParam highlightingFieldsToReturn;
    private SolrFusionRequestParam highlightQuery;
    private Query parsedHighlightQuery;
    private SolrFusionRequestParam highlightPre;
    private SolrFusionRequestParam highlightPost;
    private SolrFusionRequestParam highlight;

    private SolrFusionRequestParam facet;
    private SolrFusionRequestParam facetMincount;
    private SolrFusionRequestParam facetLimit;
    private SolrFusionRequestParam facetSort;
    private SolrFusionRequestParam facetPrefix;
    private List<SolrFusionRequestParam> facetFields;
    private List<SolrFusionRequestParam> facetSortFields;

    private SortSpec sortSpec;

    private List<String> errors;

    public static final String SORT_INDEX = "index";
    public static final String SORT_COUNT = "count";

    public FusionRequest()
    {
        start = new SolrFusionRequestParam();
        pageSize = new SolrFusionRequestParam();
        sort = new SolrFusionRequestParam();
        responseType = ResponseRendererType.JSON;
        errors = new ArrayList<>();
        query = new SolrFusionRequestParam();
        filterQuery = new ArrayList<>();
        queryFields = new SolrFusionRequestParam();
        fieldsToReturn = new SolrFusionRequestParam();
        highlightingFieldsToReturn = new SolrFusionRequestParam();
        highlightQuery = new SolrFusionRequestParam();
        highlightPre = new SolrFusionRequestParam();
        highlightPost = new SolrFusionRequestParam();
        highlight = new SolrFusionRequestParam();
        facet = new SolrFusionRequestParam();
        facetMincount = new SolrFusionRequestParam();
        facetLimit = new SolrFusionRequestParam();
        facetSort = new SolrFusionRequestParam();
        facetPrefix = new SolrFusionRequestParam();
        queryType = new SolrFusionRequestParam();
        sortSpec = new SortSpec(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE, null, false);
    }

    public Map<String, Float> getBoosts()
    {
        return new HashMap<>(); // TODO from request params
    }

    public void setResponseTypeFromString(SolrFusionRequestParam responseTypeParam, FusionRequest fusionRequest)
    {
        if (responseTypeParam != null)
        {
            String responseTypeStr = responseTypeParam.getValue();
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
    }

    public Multimap<String> buildSearchServerQueryParams(Configuration configuration,
        SearchServerConfig searchServerConfig) throws InvocationTargetException, IllegalAccessException
    {
        Multimap<String> searchServerParams = new Multimap();

        buildCoreSearchServerQueryParams(configuration, searchServerConfig, searchServerParams);

        buildHighlightSearchServerQueryParams(configuration, searchServerConfig, searchServerParams);

        buildFacetSearchServerQueryParams(configuration, searchServerConfig, searchServerParams);

        return searchServerParams;
    }

    /**
     * The map searchServerParams uses the fusion names as keys, but search server values. The solr adapter uses the
     * fusion keys to get the parameter and replaces the parameter name with the solr server's one.
     *
     * @param configuration
     * @param searchServerConfig
     * @param searchServerParams
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected void buildFacetSearchServerQueryParams(Configuration configuration, SearchServerConfig searchServerConfig,
        Multimap<String> searchServerParams) throws InvocationTargetException, IllegalAccessException
    {
        searchServerParams.put(FACET, facet);
        searchServerParams.put(FACET_LIMIT, facetLimit);
        searchServerParams.put(FACET_MINCOUNT, facetMincount);
        searchServerParams.put(FACET_PREFIX, facetPrefix);
        searchServerParams.put(FACET_SORT, facetSort);
        if (facetFields != null)
        {
            for (SolrFusionRequestParam sp : facetFields)
            {
                String p = sp.getValue();
                int curlyBracketPos = p.lastIndexOf('}');
                String prefix = "";
                if (curlyBracketPos >= 0)
                {
                    prefix = p.substring(0, curlyBracketPos + 1);
                    p = p.substring(curlyBracketPos + 1);
                }
                Set<String> searchServerFields = mapFusionFieldToSearchServerField(p, configuration, searchServerConfig,
                    null);
                for (String searchServerField : searchServerFields)
                {
                    searchServerParams.put(FACET_FIELD, prefix + searchServerField);
                }
            }
        }
        if (facetSortFields != null)
        {
            for (SolrFusionRequestParam sp : facetSortFields)
            {
                String value = sp.getValue();
                String fusionFieldName = sp.getParamNameVariablePart();
                Set<String> searchServerFields = mapFusionFieldToSearchServerField(fusionFieldName, configuration,
                    searchServerConfig, null);
                for (String searchServerField : searchServerFields)
                {
                    String facetSortField = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(searchServerField, locale);
                    searchServerParams.put(facetSortField, value);
                }
            }
        }
    }

    protected void buildCoreSearchServerQueryParams(Configuration configuration, SearchServerConfig searchServerConfig,
        Multimap<String> searchServerParams) throws InvocationTargetException, IllegalAccessException
    {
        QueryBuilderIfc queryBuilder = getQueryBuilder(configuration, searchServerConfig, false);
        QueryBuilderIfc otherQueryBuilder = getQueryBuilder(configuration, searchServerConfig, true);
        buildSearchServerQuery(parsedQuery, QUERY, configuration, searchServerConfig, searchServerParams, queryBuilder);
        buildSearchServerQuery(parsedFilterQuery, FILTER_QUERY, configuration, searchServerConfig, searchServerParams,
            otherQueryBuilder);
        buildSearchServerQuery(parsedHighlightQuery, HIGHLIGHT_QUERY, configuration, searchServerConfig,
            searchServerParams, otherQueryBuilder);
        // get all documents from 0..min(MAXDOCS,start+page size)
        if (start.isContainedInRequest())
        {
            searchServerParams.set(START, "0");
        }
        if (pageSize.isContainedInRequest())
        {
            int rows = Math.min(searchServerConfig.getMaxDocs(),
                getStart().getValueAsInt(0) + getPageSize().getValueAsInt(30));
            searchServerParams.put(PAGE_SIZE, String.valueOf(rows));
        }
        String solrFusionSortField = ResponseMapperIfc.DOC_FIELD_NAME_SCORE;
        // TODO handle 1:n mapping i.e. 1 solrfusion field is mapped to several search server fields?
        // does it mean to sort by several search server fields?
        if (sort.getValue() != null)
        {
            sortSpec = setSolrFusionSortingFromString(sort);
            Set<String> searchServerFieldSet = mapFusionFieldToSearchServerField(sortSpec.getFusionSortField(),
                configuration, searchServerConfig, ResponseMapperIfc.DOC_FIELD_NAME_SCORE);

            if (searchServerFieldSet.isEmpty())
            {
                log.error("Found not mapping for sort field '{}'", sortSpec.getFusionSortField());
            }
            String searchServerSortField = searchServerFieldSet.iterator().next();
            if (searchServerFieldSet.size() > 1)
            {
                log.error("Found ambiguous mapping {} for sort field '{}'. Using: {}", searchServerFieldSet,
                    sortSpec.getFusionSortField(), searchServerSortField);
            }
            sortSpec.setSearchServerSortField(searchServerSortField);
            if (sort.isContainedInRequest())
            {
                searchServerParams.put(SORT, searchServerSortField + (sortSpec.isSortAsc() ? " asc" : " desc"));
            }
        }
        String fusionFieldsToReturn = fieldsToReturn.getValue();
        if (fusionFieldsToReturn == null)
        {
            fusionFieldsToReturn = "*";
        }
        fusionFieldsToReturn += " " + solrFusionSortField;
        // TODO still necessary when highlighting is supported?
        if (highlightingFieldsToReturn.getValue() != null)
        {
            fusionFieldsToReturn += " " + highlightingFieldsToReturn.getValue();
        }
        String fieldsToReturn = mapFusionFieldListToSearchServerField(fusionFieldsToReturn, configuration,
            searchServerConfig, null, true);
        searchServerParams.put(FIELDS_TO_RETURN, fieldsToReturn);
        searchServerParams.put(QUERY_TYPE, queryType);
        // solrfusion wants always xml
        searchServerParams.put(WRITER_TYPE, "xml");

        if (queryFields.getValue() != null)
        {
            String mappedBoosts = mapFusionBoostFieldListToSearchServerField(queryFields.getValue(), configuration,
                searchServerConfig);
            searchServerParams.put(QUERY_FIELD, mappedBoosts);
        }
    }

    protected void addIfContained(Multimap<String> searchServerParams, SolrFusionRequestParams param,
        SolrFusionRequestParam sp, String newValue)
    {
        if (sp.isContainedInRequest())
        {
            String value = sp.getValue();
            if (newValue != null)
            {
                value = newValue;
            }
            searchServerParams.put(param, value);
        }
    }

    protected void buildHighlightSearchServerQueryParams(Configuration configuration,
        SearchServerConfig searchServerConfig, Multimap<String> searchServerParams)
    {
        String hlFieldsToReturn = mapFusionFieldListToSearchServerField(highlightingFieldsToReturn.getValue(),
            configuration, searchServerConfig, null, false);
        if (hlFieldsToReturn.length() > 0)
        {
            searchServerParams.put(HIGHLIGHT_FIELDS_TO_RETURN, hlFieldsToReturn);
        }
        searchServerParams.put(HIGHLIGHT_PRE, highlightPre);
        searchServerParams.put(HIGHLIGHT_POST, highlightPost);
        searchServerParams.put(HIGHLIGHT, highlight);
    }

    /**
     * Map a list of fusion field names to a list of search server fields. The search server's id field is automatically
     * added.
     *
     * @param fieldList          separated by SPACE or comma or combinations of them
     * @param configuration
     * @param searchServerConfig
     * @param addIdField
     * @return a string of field names separated by SPACE
     */
    protected String mapFusionFieldListToSearchServerField(String fieldList, Configuration configuration,
        SearchServerConfig searchServerConfig, String defaultSearchServerField, boolean addIdField)
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
                            mapFusionFieldToSearchServerField(fusionField, configuration, searchServerConfig,
                                defaultSearchServerField));
                    }
                }
                catch (Exception e)
                {
                    log.error("Caught exception while mapping query field {}. Ignoring field.", fusionField);
                }
            }
        }

        if (addIdField)
        {
            fieldSet.add(searchServerConfig.getIdFieldName());
        }

        return mergeFields(fieldSet);
    }

    protected String mergeFields(Set<String> fieldSet)
    {
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

    public String mapFusionBoostFieldListToSearchServerField(String fieldList, Configuration configuration,
        SearchServerConfig searchServerConfig)
    {
        Set<String> fieldSet = new LinkedHashSet<>();
        if (fieldList != null)
        {
            // support " " and "," and combinations of them as separator
            StringTokenizer st = new StringTokenizer(fieldList, " ,");
            while (st.hasMoreTokens())
            {
                String fusionFieldAndBoost = null;
                try
                {
                    fusionFieldAndBoost = st.nextToken();
                    String boost = "";
                    String fusionField = fusionFieldAndBoost;
                    int caretPos = fusionFieldAndBoost.indexOf('^');
                    if (caretPos >= 0)
                    {
                        fusionField = fusionFieldAndBoost.substring(0, caretPos);
                        boost = "^" + fusionFieldAndBoost.substring(caretPos + 1);
                    }
                    if (!fusionField.isEmpty())
                    {
                        Set<String> searchServerFields = mapFusionFieldToSearchServerField(fusionField, configuration,
                            searchServerConfig, null);
                        for (String sf : searchServerFields)
                        {
                            fieldSet.add(sf + boost);
                        }
                    }
                }
                catch (Exception e)
                {
                    log.error("Caught exception while mapping query field {}. Ignoring field.", fusionFieldAndBoost);
                }
            }
        }

        return mergeFields(fieldSet);
    }

    /**
     * Map a fusion field name to search server fields.
     *
     * @param fusionField
     * @param configuration
     * @param searchServerConfig
     * @param defaultSearchServerField
     * @return a perhaps empty set, but not "null"
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Set<String> mapFusionFieldToSearchServerField(String fusionField, Configuration configuration,
        SearchServerConfig searchServerConfig, String defaultSearchServerField)
        throws InvocationTargetException, IllegalAccessException
    {
        // preserve insertion order
        Set<String> result = new LinkedHashSet<>();
        if (fusionField.equals("*"))
        {
            result.add("*");
        }
        else
        {
            List<ApplicableResult> mappings = filterForQuery(
                searchServerConfig.findAllMappingsForFusionField(fusionField));
            boolean foundMapping = false;
            if (mappings.size() > 0)
            {
                for (ApplicableResult ar : mappings)
                {
                    foundMapping = true;
                    result.add(ar.getDestinationFieldName());
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
                if (defaultSearchServerField != null)
                {
                    result.add(defaultSearchServerField);
                    log.error(
                        "Can't handle fusion field '{}', because no mapping exist for search server {}. Using {} instead.",
                        fusionField, searchServerConfig.getSearchServerName(), result);
                }
                else
                {
                    if (!foundMapping)
                    {
                        log.warn("Didn't find mapping of fusion field '{}' for server {}.", fusionField,
                            searchServerConfig.getSearchServerName());
                    }
                }
            }
        }
        return result;
    }

    protected List<ApplicableResult> filterForQuery(List<ApplicableResult> allMappingsForFusionField)
    {
        // last <om:change> counts, because it overwrites the previous mapping
        boolean foundOkChangeMapping = false;
        for (int i = allMappingsForFusionField.size() - 1; i >= 0; i--)
        {
            boolean forQueryOk = false;
            ApplicableResult ar = allMappingsForFusionField.get(i);
            // a search server name must be present!
            if (ar.getDestinationFieldName() != null)
            {
                FieldMapping m = ar.getMapping();
                List<Operation> ops = m.getOperations();
                // no operations = <om:change>
                if (ops == null || ops.size() == 0)
                {
                    if (!foundOkChangeMapping)
                    {
                        forQueryOk = true;
                        foundOkChangeMapping = true;
                    }
                }
                else
                {
                    // find <om:change><om:query /> or <om:add level="inside"><om:query />
                    if (m.getAllAddQueryTargets(AddLevel.INSIDE).size() > 0)
                    {
                        forQueryOk = true;
                    }
                    if (!foundOkChangeMapping && m.getAllChangeQueryTargets().size() > 0)
                    {
                        forQueryOk = true;
                        foundOkChangeMapping = true;
                    }
                }
            }
            if (!forQueryOk)
            {
                allMappingsForFusionField.remove(i);
            }
        }
        return allMappingsForFusionField;
    }

    protected void buildSearchServerQuery(Query query, SolrFusionRequestParams paramName, Configuration configuration,
        SearchServerConfig searchServerConfig, Multimap<String> searchServerParams, QueryBuilderIfc queryBuilderToUse)
        throws InvocationTargetException, IllegalAccessException
    {
        if (query != null)
        {
            QueryBuilderIfc queryBuilder = queryBuilderToUse;
            Set<String> defaultSearchServerFields = mapFusionFieldToSearchServerField(
                configuration.getDefaultSearchField(), configuration, searchServerConfig, null);
            String queryStr = queryBuilder.buildQueryString(query, configuration, searchServerConfig, locale,
                defaultSearchServerFields);
            if (queryStr.length() > 0)
            {
                searchServerParams.put(paramName, queryStr);
            }
        }
    }

    public QueryBuilderIfc getQueryBuilder(Configuration configuration, SearchServerConfig searchServerConfig,
        boolean ignoreQT) throws InvocationTargetException, IllegalAccessException
    {
        boolean useDismaxQueryBuilder = MetaInfo.DISMAX_PARSER.equals(queryType.getValue());
        QueryBuilderIfc queryBuilder = null;
        if (useDismaxQueryBuilder)
        {
            queryBuilder = configuration.getDismaxQueryBuilder();
        }
        else
        {
            queryBuilder = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
        }
        return queryBuilder;
    }

    protected void buildSearchServerQuery(List<Query> queryList, SolrFusionRequestParams paramName,
        Configuration configuration, SearchServerConfig searchServerConfig, Multimap<String> searchServerParams,
        QueryBuilderIfc queryBuilderToUse) throws InvocationTargetException, IllegalAccessException
    {
        if (queryList != null)
        {
            for (Query q : queryList)
            {
                buildSearchServerQuery(q, paramName, configuration, searchServerConfig, searchServerParams,
                    queryBuilderToUse);
            }
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

    public SortSpec setSolrFusionSortingFromString(SolrFusionRequestParam sortParam)
    {
        String sortStr = sortParam.getValue();
        // "<SPACE> desc" in the case a field's name contains "desc" too
        // because sortStr is trimmed a single "desc" would be treated right
        boolean sortAsc = !sortStr.contains(" desc");
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        return new SortSpec(st.nextToken(), null, sortAsc);
    }

    protected int parseInt(String s, int defaultValue)
    {
        int result = defaultValue;
        try
        {
            result = Integer.parseInt(s);
            if (result < 0)
            {
                log.error("Ignoring negative start '{}'. Using {} instead.", s, defaultValue);
                result = defaultValue;
            }
        }
        catch (Exception e)
        {
            result = defaultValue;
            log.error("Couldn't parse '{}' to int. Using {} instead.", s, defaultValue);
        }
        return result;
    }

    /**
     * Get the sort kind ("index" or "count") for a given fusion field according to solr 4.X rules.
     *
     * @param fusionFacetField
     * @return "index" or "count"
     */
    public String getSortingOfFacetField(String fusionFacetField)
    {
        int facetLimit = -1;
        try
        {
            String facetLimitStr = getFacetLimit().getValue();
            if (facetLimitStr != null)
            {
                facetLimit = Integer.parseInt(facetLimitStr);
            }
        }
        catch (Exception e)
        {
            log.error("", e);
        }
        String sort = (facetLimit > 0) ? SORT_COUNT : SORT_INDEX;
        String generalSort = getFacetSort().getValue();
        if ("index".equals(generalSort))
        {
            sort = SORT_INDEX;
        }
        if ("count".equals(generalSort))
        {
            sort = SORT_COUNT;
        }
        if (facetSortFields != null)
        {
            for (SolrFusionRequestParam sp : facetSortFields)
            {
                if (fusionFacetField.equals(sp.getParamNameVariablePart()))
                {
                    String fieldSort = sp.getValue();
                    if ("index".equals(fieldSort))
                    {
                        sort = SORT_INDEX;
                    }
                    if ("count".equals(fieldSort))
                    {
                        sort = SORT_COUNT;
                    }
                    break;
                }
            }
        }
        return sort;
    }

    /**
     * Get the limit for a given fusion field. The default is "100". Negative numbers mean unlimited.
     *
     * @param fusionFacetField
     * @return a string containing the limit
     */
    public int getLimitOfFacetField(String fusionFacetField)
    {
        int result = 100;
        String globalFacetLimit = facetLimit.getValue();
        if (globalFacetLimit != null)
        {
            String limitStr = globalFacetLimit;
            try
            {
                result = Integer.parseInt(limitStr);
            }
            catch (Exception e)
            {
                log.error("Couldn't parse facet.limit '{}'", limitStr, e);
            }
        }
        return result;
    }

    public String getFusionSortField()
    {
        return sortSpec.getFusionSortField();
    }

    public void setFusionSortField(String s)
    {
        sortSpec.setFusionSortField(s);
    }

    public String getSearchServerSortField()
    {
        return sortSpec.getSearchServerSortField();
    }

    public void setSearchServerSortField(String s)
    {
        sortSpec.setSearchServerSortField(s);
    }

    public boolean isSortAsc()
    {
        return sortSpec.isSortAsc();
    }

    public void setSortAsc(boolean sortAsc)
    {
        sortSpec.setSortAsc(sortAsc);
    }
}
