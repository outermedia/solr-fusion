package org.outermedia.solrfusion.configuration;

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
    public Map<String, List<Target>> findAllAddQueryMappings(AddLevel level, QueryTarget target)
    {
        Map<String, List<Target>> result = null;
        if (result == null)
        {
            // preserve order
            result = new LinkedHashMap<>();
            for (FieldMapping m : fieldMappings)
            {
                List<Target> queryTargets = m.getAllAddQueryTargets(level, target);
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
        }
        return result;
    }

    /**
     * Get all response mappings which add something.
     *
     * @return a perhaps empty table of all response parts to add.
     */
    public Map<String, TargetsOfMapping> findAllAddResponseMappings(ResponseTarget target)
    {
        Map<String, TargetsOfMapping> result = null;
        if (result == null)
        {
            // preserve order
            result = new LinkedHashMap<>();
            for (FieldMapping m : fieldMappings)
            {
                TargetsOfMapping responseTargets = m.getAllAddResponseTargets(target);
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
        }
        return result;
    }
}
