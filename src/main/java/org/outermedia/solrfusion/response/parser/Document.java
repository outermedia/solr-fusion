package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Data holder class keeping the a response document.
 *
 * @author stephan
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentType")
@Getter
@Setter
@ToString
public class Document implements VisitableDocument
{
    @XmlElements(value =
            {
                    @XmlElement(name = "str"),
                    @XmlElement(name = "date"),
                    @XmlElement(name = "float"),
                    @XmlElement(name = "long"),
            })
    private List<SolrSingleValuedField> solrSingleValuedFields;

    @XmlElement(name = "arr", required = true)
    private List<SolrMultiValuedField> solrMultiValuedFields;

    /**
     * Find a field of any type by name.
     *
     * @param name solrname of the field
     * @return null or an instnace of {@link SolrSingleValuedField}
     */
    public SolrField findFieldByName(final String name)
    {
        return accept(new FieldVisitor()
        {
            @Override
            public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
            {
                return !sf.getFieldName().equals(name);
            }

            @Override
            public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
            {
                return !sf.getFieldName().equals(name);
            }
        }, null);
    }

    @Override
    public SolrField accept(FieldVisitor visitor, ScriptEnv env)
    {
        if (solrSingleValuedFields != null)
        {
            for (SolrSingleValuedField solrField : solrSingleValuedFields)
            {
                if (!visitor.visitField(solrField, env))
                {
                    return solrField;
                }
            }
        }
        if (solrMultiValuedFields != null)
        {
            for (SolrMultiValuedField solrField : solrMultiValuedFields)
            {
                if (!visitor.visitField(solrField, env))
                {
                    return solrField;
                }
            }
        }
        return null;
    }

    /**
     * Get a document's field value by name. For multi value fields, all values are returned. Note: An instance of class
     * Term contains the original and mapped value.
     *
     * @param fieldName the field's name for which to return the value
     * @return null if field was not found or the Term of the field
     */
    public Term getFieldTermByName(String fieldName)
    {
        Term result = null;
        SolrField sf = findFieldByName(fieldName);
        if (sf != null)
        {
            result = sf.getTerm();
        }
        return result;
    }

}