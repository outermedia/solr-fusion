package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;

import java.util.List;

/**
 * Data holder class to represent a multivalued or singlevalued field in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerSingleValuedField
{

    @Getter
    private String name;

    @Getter
    private String type;

    @Getter
    private String value;

    public static FreemarkerSingleValuedField fromSolrSingleValuedField(SolrSingleValuedField sf)
    {
        FreemarkerSingleValuedField freemarkerField = null;
        Term t = sf.getTerm();

        String v = null;
        List<String> fusionFieldValues = t.getFusionFieldValue();

        if (t.isWasMapped() && !t.isRemoved())
        {
            if (fusionFieldValues != null && !fusionFieldValues.isEmpty())
            {
                v = fusionFieldValues.get(0);
            }
            freemarkerField = new FreemarkerSingleValuedField(t.getFusionFieldName(), t.getFusionField().getType(), v);
        }
        return freemarkerField;
    }

    private FreemarkerSingleValuedField(String name, String type, String value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}