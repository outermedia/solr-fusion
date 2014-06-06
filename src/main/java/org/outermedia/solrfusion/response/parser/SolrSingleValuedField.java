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
    public String value;

    @XmlTransient
    private Term term;

    @Override
    public List<Term> getTerms ()
    {
        List<Term> termList = new ArrayList<>();
        termList.add(this.term);
        return termList;
    }

    @Override
    public List<String> getValues ()
    {
        List<String> l = new ArrayList<>();
        l.add(this.value);
        return l;
    }

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
