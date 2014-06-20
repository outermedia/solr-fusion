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

    @Getter private List<FreemarkerMultiValuedField> multiValuedFields;
    @Getter private List<FreemarkerSingleValuedField> singleValuedFields;

    @Getter private boolean hasMultiValuedFields;
    @Getter private boolean hasSingleValuedFields;

    public FreemarkerDocument()
    {
        this.multiValuedFields = new ArrayList<>();
        this.singleValuedFields = new ArrayList<>();
    }

    @Override
    public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
    {
        FreemarkerSingleValuedField freemarkerField = FreemarkerSingleValuedField.fromSolrSingleValuedField(sf);
        if (freemarkerField != null)
        {
            singleValuedFields.add(freemarkerField);
            hasSingleValuedFields = true;
        }
        return true;
    }

    @Override
    public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
    {
        FreemarkerMultiValuedField freemarkerField = FreemarkerMultiValuedField.fromSolrMultiValuedField(msf);
        if (freemarkerField != null)
        {
            multiValuedFields.add(freemarkerField);
            hasMultiValuedFields = true;
        }
        return true;
    }

}

