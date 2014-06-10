package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
            public boolean visitField(SolrField sf, ScriptEnv env)
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
            for (SolrField solrField : solrSingleValuedFields)
            {
                if (!visitor.visitField(solrField, env))
                {
                    return solrField;
                }
            }
        }
        if (solrMultiValuedFields != null)
        {
            for (SolrField solrField : solrMultiValuedFields)
            {
                if (!visitor.visitField(solrField, env))
                {
                    return solrField;
                }
            }
        }
        return null;
    }
}