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
import org.outermedia.solrfusion.FusionControllerIfc;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.QueryMapperIfc;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;

import javax.xml.bind.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Data holder class which stores the SolrFusion schema.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "",
         propOrder = {
             "fusionFields", "scriptTypes", "defaultSearchField", "defaultSortField", "defaultOperator",
             "idGeneratorFactory", "responseConsolidatorFactory", "responseMapperFactory", "queryMapperFactory",
             "controllerFactory", "searchServerConfigs"
         })
@XmlRootElement(name = "core",
                namespace = "http://solrfusion.outermedia.org/configuration/")
@ToString
public class Configuration
{
    @XmlElement(name = "fusion-schema-fields",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private FusionFieldList fusionFields;

    @XmlElement(name = "script-type",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private List<ScriptType> scriptTypes;

    @XmlElement(name = "default-search-field",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private String defaultSearchField;

    @XmlElement(name = "default-sort-field",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private String defaultSortField;

    @XmlElement(name = "default-operator",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private String defaultOperator;

    @XmlElement(name = "id-generator",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private IdGeneratorFactory idGeneratorFactory;

    @XmlElement(name = "response-consolidator",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private ResponseConsolidatorFactory responseConsolidatorFactory;

    @XmlElement(name = "response-mapper",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private ResponseMapperFactory responseMapperFactory;

    @XmlElement(name = "query-mapper",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private QueryMapperFactory queryMapperFactory;

    @XmlElement(name = "controller",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private ControllerFactory controllerFactory;

    @XmlElement(name = "solr-servers",
                namespace = "http://solrfusion.outermedia.org/configuration/",
                required = true)
    @Getter
    @Setter
    private GlobalSearchServerConfig searchServerConfigs;

    /**
     * Find a response renderer by type.
     *
     * @param type
     *     is either PHP, JSON or XML (see {@link ResponseRendererType})
     * @return null for an error or an instance of {@link ResponseRendererIfc}
     */
    public ResponseRendererIfc getResponseRendererByType(ResponseRendererType type)
        throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getResponseRendererByType(type);
    }

    /**
     * Get the response timeout in seconds.
     *
     * @return a number greater 0.
     */
    public int getTimeout()
    {
        return searchServerConfigs.getTimeout();
    }

    /**
     * The minimal number of search servers which have to respond. If too few respond the disaster message (see {@link
     * #getDisasterMessage()}) is thrown.
     *
     * @return the minimal number of responding servers
     */
    public int getDisasterLimit()
    {
        return searchServerConfigs.getDisasterLimit();
    }

    /**
     * Get the disaster message (see {@link #getDisasterLimit()}) when too few server respond.
     *
     * @return a Message object which contains a key and the text
     */
    public Message getDisasterMessage()
    {
        return searchServerConfigs.getDisasterMessage();
    }

    /**
     * Get the query parser instance.
     *
     * @return an instance of QueryParserIfc
     */
    public QueryParserIfc getQueryParser() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getQueryParser();
    }

    /**
     * Get the default response parser. Please note that {@link SearchServerAdapterIfc} can use a special response
     * parser.
     *
     * @return an instance of ResponseParserIfc.
     */
    public ResponseParserIfc getDefaultResponseParser() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getDefaultResponseParser();
    }

    /**
     * Get the default query builder. Please note that {@link SearchServerAdapterIfc} can use a special query builder.
     *
     * @return an instance of QueryBuilderIfcs.
     */
    public QueryBuilderIfc getDefaultQueryBuilder() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getDefaultQueryBuilder();
    }

    /**
     * Get the optionally declared merge strategy.
     *
     * @return an instance of MergeStrategyIfc
     */
    public MergeStrategyIfc getMerger() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getMerger();
    }


    /**
     * Get the response consolidator instance.
     *
     * @return an instance of ResponseConsolidatorIfc
     */
    public ResponseConsolidatorIfc getResponseConsolidator(Configuration config)
        throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc c = getResponseConsolidatorFactory().getInstance();
        c.initConsolidator(config);
        return c;
    }

    /**
     * Get all configured search servers. Every call of this method returns a new list object, but the {@link
     * SearchServerAdapterIfc} instances are re-used.
     *
     * @return a list of SearchServerAdapterIfc
     */
    public List<SearchServerAdapterIfc> getSearchServers() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getSearchServers();
    }

    /**
     * Get the configuration of all configured search servers.
     *
     * @return a list of SearchServerConfig
     */
    public List<SearchServerConfig> getConfigurationOfSearchServers()
    {
        return searchServerConfigs.getSearchServerConfigs();
    }

    public FusionField findFieldByName(String name)
    {
        for (FusionField ff : fusionFields.getFusionFields())
        {
            if (ff.getFieldName().equalsIgnoreCase(name))
            {
                return ff;
            }
        }
        return null;
    }

    /**
     * Get the configured id generator.
     *
     * @return a non null instance of IdGeneratorIfc
     */
    public IdGeneratorIfc getIdGenerator() throws InvocationTargetException, IllegalAccessException
    {
        return idGeneratorFactory.getInstance();
    }

    /**
     * Get the configured response mapper.
     *
     * @return a non null instance of ResponseMapperIfc
     */
    public ResponseMapperIfc getResponseMapper() throws InvocationTargetException, IllegalAccessException
    {
        return responseMapperFactory.getInstance();
    }

    /**
     * Get the configured query mapper.
     *
     * @return a non null instance of QueryMapperIfc
     */
    public QueryMapperIfc getQueryMapper() throws InvocationTargetException, IllegalAccessException
    {
        return queryMapperFactory.getInstance();
    }

    /**
     * Get the configured controller.
     *
     * @return a non null instance of FusionControllerIfc
     */
    public FusionControllerIfc getController() throws InvocationTargetException, IllegalAccessException
    {
        return controllerFactory.getInstance();
    }

    /**
     * Get the default page size.
     *
     * @return a number
     */
    public int getDefaultPageSize()
    {
        return searchServerConfigs.getDefaultPageSize();
    }

    /**
     * Get a search server's configuration by the server's id.
     *
     * @param fusionDocId
     * @return null or an object
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public SearchServerConfig getSearchServerConfigByFusionDocId(String fusionDocId)
        throws InvocationTargetException, IllegalAccessException
    {
        String searchServerId = getIdGenerator().getSearchServerIdFromFusionId(fusionDocId);
        return searchServerConfigs.getSearchServerConfigById(searchServerId);
    }

    /**
     * Get a search server's configuration by server name.
     *
     * @param serverName
     * @return null or an object
     */
    public SearchServerConfig getSearchServerConfigByName(String serverName)
    {
        return searchServerConfigs.getSearchServerConfigById(serverName);
    }

    public String getFusionIdFieldName() throws InvocationTargetException, IllegalAccessException
    {
        return getIdGeneratorFactory().getInstance().getFusionIdField();
    }

    public ScriptType findScriptTypeByName(String name)
    {
        ScriptType result = null;
        for (ScriptType st : scriptTypes)
        {
            if (st.getName().equals(name))
            {
                result = st;
                break;
            }
        }
        return result;
    }

    public QueryBuilderIfc getDismaxQueryBuilder() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getDismaxQueryBuilder();
    }

    public QueryParserIfc getDismaxQueryParser() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getDismaxQueryParser();
    }

    public List<String> allSearchServerNames()
    {
        return searchServerConfigs.allSearchServerNames();
    }
}
