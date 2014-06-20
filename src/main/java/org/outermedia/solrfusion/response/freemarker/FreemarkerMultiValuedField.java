package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;

import java.util.List;

/**
 * Data holder class to represent a multivalued or singlevalued field in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerMultiValuedField
{

    @Getter
    private String name;

    @Getter
    private String type;

    @Getter
    private List<String> values;

    public static FreemarkerMultiValuedField fromSolrMultiValuedField(SolrMultiValuedField msf)
    {
        FreemarkerMultiValuedField freemarkerField = null;

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
                freemarkerField = new FreemarkerMultiValuedField(t.getFusionFieldName(), t.getFusionField().getType(), t.getFusionFieldValue());
            }
        }
        return freemarkerField;

    }

    private FreemarkerMultiValuedField(String name, String type, List<String> values)
    {
        this.name = name;
        this.type = type;
        this.values = values;
    }
}