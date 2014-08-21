package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Base Class for SolrSingleValuedField and SolrMultiValuedField to be accessed uniformly.
 *
 * @author stephan
 */

@XmlTransient
@ToString(exclude = {"term"})
public abstract class SolrField
{

    @XmlAttribute(name = "name", required = true) @Getter @Setter
    protected String fieldName;

    @XmlTransient @Getter @Setter
    private Term term;

    public String getFirstSearchServerFieldValue()
    {
        String result = null;
        List<String> allValues = term.getSearchServerFieldValue();
        if (allValues != null && allValues.size() > 0)
        {
            result = allValues.get(0);
        }
        return result;
    }

    public List<String> getAllSearchServerFieldValue()
    {
        List<String> result = null;
        List<String> values = term.getSearchServerFieldValue();
        if (values != null)
        {
            result = new ArrayList<>(values);
        }
        return result;
    }

    public String getFirstFusionFieldValue()
    {
        String result = null;
        List<String> allValues = term.getFusionFieldValue();
        if (allValues != null && allValues.size() > 0)
        {
            result = allValues.get(0);
        }
        return result;

    }

    public List<String> getAllFusionFieldValue()
    {
        List<String> result = new ArrayList<>();
        List<String> values = term.getFusionFieldValue();
        if (values != null)
        {
            result.addAll(values);
        }
        return result;
    }

    public boolean isFusionField(String fusionFieldName)
    {
        return fusionFieldName.equals(term.getFusionFieldName());
    }

    public List<Integer> getFusionFacetCount()
    {
        return term.getFusionFacetCount();
    }

    public String getFusionFieldName()
    {
        return term.getFusionFieldName();
    }

    public boolean isProcessed() { return term.isProcessed(); }

    public boolean isRemoved() { return term.isRemoved(); }

    public List<Integer> getSearchServerFacetWordCounts()
    {
        return term.getSearchServerFacetCount();
    }
}
