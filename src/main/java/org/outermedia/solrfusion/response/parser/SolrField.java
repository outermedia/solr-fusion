package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/**
 * Data holder class keeping the field information of a solr response document.
 *
 * @author stephan
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldType")
@Getter
@Setter
@ToString
public class SolrField
{

    @XmlAttribute(name = "name", required = true)
    private String fieldName;

    @XmlValue
    private String value;

    @XmlTransient
    private Term term;

    /**
     * Hook up unmarshalling in order to create an instance of
     * {@link org.outermedia.solrfusion.mapper.Term}.
     *
     * @param u      is the unmarshaller
     * @param parent the parent object
     */
    protected void afterUnmarshal(Unmarshaller u, Object parent)
    {
        term = Term.newSearchServerTerm(fieldName, value);
    }
}
