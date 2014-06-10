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
@Getter
@Setter
@ToString
public class SolrMultiValuedField extends SolrField
{

    @XmlElements(value =
            {
                    @XmlElement(name = "str"),
                    @XmlElement(name = "date"),
                    @XmlElement(name = "float"),
                    @XmlElement(name = "long"),
            })
    private List<String> values;

    @XmlTransient
    private List<Term> terms;

    /**
     * Hook up unmarshalling in order to create an instance of
     * {@link org.outermedia.solrfusion.mapper.Term}.
     *
     * @param u      is the unmarshaller
     * @param parent the parent object
     */
    protected void afterUnmarshal(Unmarshaller u, Object parent)
    {
        terms = new ArrayList<>();

        for (String value : this.getValues())
        {
            if (!value.isEmpty())
            {
                terms.add(Term.newSearchServerTerm(fieldName, value));
            }
        }
    }

    @Override
    public String getFirstSearchServerFieldValue()
    {
        String result = null;
        if (values != null && !values.isEmpty())
        {
            result = values.get(0);
        }
        return result;
    }

    @Override
    public List<String> getAllSearchServerFieldValue()
    {
        return values;
    }
}
