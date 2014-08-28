package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.ScoreCorrectorIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;

import javax.xml.bind.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Data holder keeping one search server's configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/",
    propOrder = {"url", "scoreFactory", "responseParserFactory", "queryBuilderFactory", "idFieldName", "maxDocs", "fieldMappings"})
@Getter
@Setter
@ToString(callSuper = true, exclude = {"allAddQueryMappingsCache", "allAddResponseMappingsCache"})
public class SearchServerConfig extends ConfiguredFactory<SearchServerAdapterIfc, SearchServerConfig>
{
    @XmlAttribute(name = "name", required = true)
    private String searchServerName;

    @XmlAttribute(name = "query-param-name", required = false)
    private String queryParamName;

    @XmlAttribute(name = "version", required = true)
    private String searchServerVersion;

    @XmlAttribute(name = "enabled", required = false)
    private Boolean enabled = true;

    @XmlElement(name = "url", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private String url;

    @XmlElement(name = "score", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private ScoreFactory scoreFactory;

    @XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/",
        required = false)
    private ResponseParserFactory responseParserFactory;

    @XmlElement(name = "query-builder", namespace = "http://solrfusion.outermedia.org/configuration/", required = false)
    private QueryBuilderFactory queryBuilderFactory;

    @XmlElement(name = "unique-key", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private String idFieldName;

    @XmlElement(name = "max-docs", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private int maxDocs;

    @XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<FieldMapping> fieldMappings;

    @XmlTransient
    private Map<String, List<Target>> allAddQueryMappingsCache;

    @XmlTransient
    private Map<String, TargetsOfMapping> allAddResponseMappingsCache;


    /**
     * Get all mappings for a given fusion field name.
     *
     * @param fusionFieldName is the field for which mappings shall be returned
     * @return a list with mappings, perhaps empty
     */
    public List<ApplicableResult> findAllMappingsForFusionField(String fusionFieldName)
    {
        List<ApplicableResult> result = new ArrayList<>();
        for (FieldMapping m : fieldMappings)
        {
            ApplicableResult applicable = m.applicableToFusionField(fusionFieldName);
            if (applicable != null)
            {
                result.add(applicable);
            }
        }
        return result;
    }

    /**
     * Get all mappings for a given search server field name.
     *
     * @param searchServerFieldName is the field for which mappings shall be returned
     * @return a list with mappings, perhaps empty
     */
    public List<ApplicableResult> findAllMappingsForSearchServerField(String searchServerFieldName)
    {
        List<ApplicableResult> result = new ArrayList<>();
        for (FieldMapping m : fieldMappings)
        {
            ApplicableResult applicable = m.applicableToSearchServerField(searchServerFieldName);
            if (applicable != null)
            {
                result.add(applicable);
            }
        }
        return result;
    }

    public ScoreCorrectorIfc getScoreCorrector() throws InvocationTargetException, IllegalAccessException
    {
        return getScoreFactory().getInstance();
    }

    public ResponseParserIfc getResponseParser(ResponseParserIfc defaultResponseParser)
        throws InvocationTargetException, IllegalAccessException
    {
        ResponseParserIfc result = defaultResponseParser;
        if (responseParserFactory != null)
        {
            result = responseParserFactory.getInstance();
        }
        return result;
    }

    public QueryBuilderIfc getQueryBuilder(QueryBuilderIfc defaultQueryBuilder)
        throws InvocationTargetException, IllegalAccessException
    {
        QueryBuilderIfc result = defaultQueryBuilder;
        if (queryBuilderFactory != null)
        {
            result = queryBuilderFactory.getInstance();
        }
        return result;
    }

    /**
     * Get all query mappings which add something.
     *
     * @return a perhaps empty table of all query parts to add.
     * @param level
     */
    public Map<String, List<Target>> findAllAddQueryMappings(AddLevel level)
    {
        Map<String, List<Target>> result = allAddQueryMappingsCache;
        if (result == null)
        {
            // preserve order
            result = new LinkedHashMap<>();
            for (FieldMapping m : fieldMappings)
            {
                List<Target> queryTargets = m.getAllAddQueryTargets(level);
                if (queryTargets.size() > 0)
                {
                    List<Target> existingQueryTargets = result.get(m.getSearchServersName());
                    if (existingQueryTargets == null)
                    {
                        result.put(m.getSearchServersName(), queryTargets);
                    }
                    else
                    {
                        existingQueryTargets.addAll(queryTargets);
                    }
                }
            }
            allAddQueryMappingsCache = result;
        }
        return result;
    }

    /**
     * Get all response mappings which add something.
     *
     * @return a perhaps empty table of all response parts to add.
     */
    public Map<String, TargetsOfMapping> findAllAddResponseMappings()
    {
        Map<String, TargetsOfMapping> result = allAddResponseMappingsCache;
        if (result == null)
        {
            // preserve order
            result = new LinkedHashMap<>();
            for (FieldMapping m : fieldMappings)
            {
                TargetsOfMapping responseTargets = m.getAllAddResponseTargets();
                if (responseTargets.size() > 0)
                {
                    List<Target> existingTargets = result.get(m.getFusionName());
                    if (existingTargets == null)
                    {
                        result.put(m.getFusionName(), responseTargets);
                    }
                    else
                    {
                        existingTargets.addAll(responseTargets);
                    }
                }
            }
            allAddResponseMappingsCache = result;
        }
        return result;
    }
}
