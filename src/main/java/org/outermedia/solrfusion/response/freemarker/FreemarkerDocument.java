package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder class to represent a Document in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerDocument  implements FieldVisitor
{

    @Getter private List<FreemarkerField> fields;

    public FreemarkerDocument()
    {
        this.fields = new ArrayList<>();
    }

    @Override
    public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
    {
        FreemarkerField freemarkerField = FreemarkerField.FreemarkerFieldFromSolrSingleValuedField(sf);
        if (freemarkerField != null)
        {
            fields.add(freemarkerField);
        }
        return true;
    }

    @Override
    public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
    {
        FreemarkerField freemarkerField = FreemarkerField.FreemarkerFieldFromSolrMultiValuedField(msf);
        if (freemarkerField != null)
        {
            fields.add(freemarkerField);
        }
        return true;
    }

}

