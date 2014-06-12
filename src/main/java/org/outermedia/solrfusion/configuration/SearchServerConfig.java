package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.ScoreCorrectorIfc;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;

import javax.xml.bind.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data holder keeping one search server's configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchServerConfig", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "url", "scoreFactory", "responseParserFactory", "idFieldName",
                "fieldMappings"
        })
@Getter
@Setter
@ToString(callSuper = true)
public class SearchServerConfig extends
        ConfiguredFactory<SearchServerAdapterIfc, SearchServerConfig>
{
    @XmlAttribute(name = "name", required = true)
    private String searchServerName;

    @XmlAttribute(name = "version", required = true)
    private String searchServerVersion;

    @XmlElement(name = "url", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private String url;

    @XmlElement(name = "score", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private ScoreFactory scoreFactory;

    @XmlElement(name = "response-parser", namespace = "http://solrfusion.outermedia.org/configuration/", required = false)
    private ResponseParserFactory responseParserFactory;

    @XmlElement(name = "unique-key", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private String idFieldName;

    @XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<FieldMapping> fieldMappings;

    /**
     * Get all mappings for a given fusion field name.
     *
     * @param fusionFieldName is the field for which mappings shall be returned
     * @return a list with mappings, perhaps empty
     */
    public List<FieldMapping> findAllMappingsForFusionField(String fusionFieldName)
    {
        List<FieldMapping> result = new ArrayList<>();
        for (FieldMapping m : fieldMappings)
        {
            if (m.applicableToFusionField(fusionFieldName))
            {
                result.add(m);
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
    public List<FieldMapping> findAllMappingsForSearchServerField(String searchServerFieldName)
    {
        List<FieldMapping> result = new ArrayList<>();
        for (FieldMapping m : fieldMappings)
        {
            if (m.applicableToSearchServerField(searchServerFieldName))
            {
                result.add(m);
            }
        }
        return result;
    }

    public ScoreCorrectorIfc getScoreCorrector() throws InvocationTargetException, IllegalAccessException
    {
        return getScoreFactory().getInstance();
    }

}
