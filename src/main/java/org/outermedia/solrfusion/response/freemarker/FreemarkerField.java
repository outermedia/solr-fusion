package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder class to represent a multivalued or singlevalued field in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerField
{

    @Getter
    private String name;

    @Getter
    private String type;

    @Getter
    private String value;

    @Getter
    private boolean isMultiValued;

    @Getter private List<FreemarkerField> subfields;

    public static FreemarkerField FreemarkerFieldFromSolrMultiValuedField(SolrMultiValuedField msf)
    {
        FreemarkerField freemarkerField = null;

        Term t = msf.getTerm();
        if (t != null)
        {
            boolean printNone = true;
            if (t.isWasMapped() && !t.isRemoved())
            {
                printNone = false;
            }

            if (!printNone)
            {
                List<FreemarkerField> subfields = new ArrayList<>();
                for (String v : t.getFusionFieldValue())
                {
                    freemarkerField = new FreemarkerField(t.getFusionFieldName(), t.getFusionField().getType(), v);
                    subfields.add(freemarkerField);
                }
                freemarkerField = new FreemarkerField(t.getFusionFieldName(), null, null, true, subfields);
            }
        }

        return freemarkerField;
    }

    public static FreemarkerField FreemarkerFieldFromSolrSingleValuedField(SolrSingleValuedField sf)
    {
        FreemarkerField freemarkerField = null;
        Term t = sf.getTerm();

        String v = null;
        List<String> fusionFieldValues = t.getFusionFieldValue();

        if (t.isWasMapped() && !t.isRemoved())
        {
            if (fusionFieldValues != null && !fusionFieldValues.isEmpty())
            {
                v = fusionFieldValues.get(0);
            }
            freemarkerField = new FreemarkerField(t.getFusionFieldName(), t.getFusionField().getType(), v);
        }
        return freemarkerField;
    }

    private FreemarkerField(String name, String type, String value, boolean isMultiValued, List<FreemarkerField> subfields)
    {
        this.name = name;
        this.type = type;
        this.value = value;
        this.isMultiValued = isMultiValued;
        this.subfields = subfields;
    }

    private FreemarkerField(String name, String type, String value)
    {
        this(name, type, value, false, null);
    }
}