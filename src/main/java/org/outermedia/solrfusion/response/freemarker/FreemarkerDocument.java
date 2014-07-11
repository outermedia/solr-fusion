package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.FusionField;
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
@Slf4j
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
        FusionField field = env.getConfiguration().findFieldByName(sf.getTerm().getFusionFieldName());
        if (field == null)
            return true;
//        field.getFieldType();

        if (field.isMultiValue())
        {
            // fusion-field is configured as multivalue, but solr server gave a single valued field
            FreemarkerMultiValuedField freemarkerField = FreemarkerMultiValuedField.fromSolrField(sf);
            addMultiValuedField(freemarkerField);
        }
        else
        {
            FreemarkerSingleValuedField freemarkerField = FreemarkerSingleValuedField.fromSolrField(sf);
            addSingleValuedField(freemarkerField);
        }

        return true;
    }

    @Override
    public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
    {
        FusionField field = env.getConfiguration().findFieldByName(sf.getTerm().getFusionFieldName());
//        field.getFieldType();
        if (field == null)
            return true;

        if (field.isSingleValue())
        {
            // error in mapping. will ne logged and nothing is rendered
            log.error("Unable to render multiple values in single valued field {}", field.getFieldName());
            return true;
        }

        FreemarkerMultiValuedField freemarkerField = FreemarkerMultiValuedField.fromSolrField(sf);
        addMultiValuedField(freemarkerField);
        return true;
    }

    private void addMultiValuedField(FreemarkerMultiValuedField freemarkerField) {
        if (freemarkerField != null)
        {
            multiValuedFields.add(freemarkerField);
            hasMultiValuedFields = true;
        }
    }

    private void addSingleValuedField(FreemarkerSingleValuedField freemarkerField) {
        if (freemarkerField != null)
        {
            singleValuedFields.add(freemarkerField);
            hasSingleValuedFields = true;
        }
    }

}

