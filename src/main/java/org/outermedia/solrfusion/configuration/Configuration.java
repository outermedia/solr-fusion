package org.outermedia.solrfusion.configuration;

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
 * Data holder class keeping the fusion configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
        {
                "fusionFields", "scriptTypes", "defaultSearchField", "defaultOperator",
                "idGeneratorFactory", "responseConsolidatorFactory", "responseMapperFactory",
                "queryMapperFactory", "controllerFactory",
                "searchServerConfigs"
        })
@XmlRootElement(name = "core", namespace = "http://solrfusion.outermedia.org/configuration/")
@ToString
public class Configuration
{
    @XmlElement(name = "fusion-schema-fields", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private FusionFieldList fusionFields;

    @XmlElement(name = "script-type", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private List<ScriptType> scriptTypes;

    @XmlElement(name = "default-search-field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private String defaultSearchField;

    @XmlElement(name = "default-operator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private String defaultOperator;

    @XmlElement(name = "id-generator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private IdGeneratorFactory idGeneratorFactory;

    @XmlElement(name = "response-consolidator", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private ResponseConsolidatorFactory responseConsolidatorFactory;

    @XmlElement(name = "response-mapper", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private ResponseMapperFactory responseMapperFactory;

    @XmlElement(name = "query-mapper", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private QueryMapperFactory queryMapperFactory;

    @XmlElement(name = "controller", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private ControllerFactory controllerFactory;

    @XmlElement(name = "solr-servers", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    @Getter
    @Setter
    private GlobalSearchServerConfig searchServerConfigs;

    /**
     * Find a response renderer by type.
     *
     * @param type is either PHP, JSON or XML (see {@link ResponseRendererType})
     * @return null for an error or an instance of {@link ResponseRendererIfc}
     */
    public ResponseRendererIfc getResponseRendererByType(
            ResponseRendererType type) throws InvocationTargetException, IllegalAccessException
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
     * The minimal number of search servers which have to respond. If too few
     * respond the disaster message (see {@link #getDisasterMessage()}) is
     * thrown.
     *
     * @return the minimal number of responding servers
     */
    public int getDisasterLimit()
    {
        return searchServerConfigs.getDisasterLimit();
    }

    /**
     * Get the disaster message (see {@link #getDisasterLimit()}) when too few
     * server respond.
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
     * Get the default response parser. Please note that
     * {@link SearchServerAdapterIfc} can use a special response parser.
     *
     * @return an instance of ResponseParserIfc.
     */
    public ResponseParserIfc getDefaultResponseParser() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfigs.getDefaultResponseParser();
    }

    /**
     * Get the default query builder. Please note that
     * {@link SearchServerAdapterIfc} can use a special query builder.
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
    public ResponseConsolidatorIfc getResponseConsolidator() throws InvocationTargetException, IllegalAccessException
    {
        return getResponseConsolidatorFactory().getInstance();
    }

    /**
     * Get all configured search servers. Every call of this method returns a
     * new list object, but the {@link SearchServerAdapterIfc} instances are
     * re-used.
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
            if (ff.getFieldName().equals(name))
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
}
