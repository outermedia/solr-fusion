package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
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
    @XmlElement(name = "float", required = false)
    private List<SolrField> solrFloatFields;

    @XmlElement(name = "str", required = true)
    private List<SolrField> solrStringFields;

    /**
     * Find a field of any type by name.
     *
     * @param name solrname of the field
     * @return null or an instnace of {@link org.outermedia.solrfusion.response.parser.SolrField}
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
        if (solrFloatFields != null)
        {
            for (SolrField solrField : solrFloatFields)
            {
                if (!visitor.visitField(solrField, env))
                {
                    return solrField;
                }
            }
        }
        if (solrStringFields != null)
        {
            for (SolrField solrField : solrStringFields)
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