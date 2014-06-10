package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

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
public class SolrSingleValuedField extends SolrField
{

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

    @Override
    public String getFirstSearchServerFieldValue()
    {
        return value;
    }

    @Override
    public List<String> getAllSearchServerFieldValue()
    {
        List<String> result = new ArrayList<>(1);
        result.add(value);
        return result;
    }
}
