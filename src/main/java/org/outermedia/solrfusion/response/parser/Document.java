package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Data holder class keeping the a response document.
 *
 * @author stephan
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentType")
@Getter
@Setter
@ToString
public class Document
{
    @XmlElement (name = "float", required = false)
    private List<SolrField> solrFloatFields;

    @XmlElement(name = "str", required = true)
    private List<SolrField> solrStringFields;

    /**
     * Find a field of any type by name.
     *
     * @param name solrname of the field
     * @return null or an instnace of {@link org.outermedia.solrfusion.response.parser.SolrField}
     */
    public SolrField findFieldByName(String name)
    {
        for (SolrField solrField : solrFloatFields)
        {
            if (solrField.getFieldName().equals(name)) return solrField;
        }
        for (SolrField solrField : solrStringFields)
        {
            if (solrField.getFieldName().equals(name)) return solrField;
        }
        return null;
    }
}