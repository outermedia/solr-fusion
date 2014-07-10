package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Find a field of any type by search server name.
     *
     * @param name solrname of the field
     * @return null or an instance of {@link SolrSingleValuedField}
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

    /**
     * Find a field of any type by fusion name.
     *
     * @param fusionName fusion name of the field
     * @return null or an instance of {@link SolrSingleValuedField}
     */
    public SolrField findFieldByFusionName(final String fusionName)
    {
        return accept(new FieldVisitor()
        {
            @Override
            public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
            {
                return !fusionName.equals(sf.getTerm().getFusionFieldName());
            }

            @Override
            public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
            {
                return !fusionName.equals(sf.getTerm().getFusionFieldName());
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
     * Get a document's field value by a search server name. Note: An instance of class
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

    /**
     * Get a document's field value by a fusion name. Note: An instance of class
     * Term contains the original and mapped value.
     *
     * @param fusionFieldName the field's name for which to return the value
     * @return null if field was not found or the Term of the field
     */
    public Term getFieldTermByFusionName(String fusionFieldName)
    {
        Term result = null;
        SolrField sf = findFieldByFusionName(fusionFieldName);
        if (sf != null)
        {
            result = sf.getTerm();
        }
        return result;
    }

    /**
     * Convenient method to build easier build responses. Especially useful in unit tests.
     *
     * @param name
     * @param value
     * @return
     */
    public SolrField addField(String name, String... value)
    {
        SolrField result = null;
        if (value.length == 1)
        {
            SolrSingleValuedField f = new SolrSingleValuedField();
            f.setFieldName(name);
            f.setValue(value[0]);
            f.setTerm(Term.newSearchServerTerm(f.getFieldName(), f.getValue()));
            if (solrSingleValuedFields == null)
            {
                solrSingleValuedFields = new ArrayList<>();
            }
            solrSingleValuedFields.add(f);
            result = f;
        }
        else if (value.length > 1)
        {
            SolrMultiValuedField f = new SolrMultiValuedField();
            f.setFieldName(name);
            List<String> vals = new ArrayList<>();
            vals.addAll(Arrays.asList(value));
            f.setValues(vals);
            f.setTerm(Term.newSearchServerTerm(f.getFieldName(), f.getValues()));
            if (solrMultiValuedFields == null)
            {
                solrMultiValuedFields = new ArrayList<>();
            }
            solrMultiValuedFields.add(f);
            result = f;
        }
        else
        {
            throw new RuntimeException("No value given for field: '" + name + "'");
        }
        return result;
    }

}