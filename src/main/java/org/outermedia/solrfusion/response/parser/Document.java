package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.configuration.FusionField;
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
@Slf4j
public class Document implements VisitableDocument
{
    @XmlElements(
        value = {@XmlElement(name = "str"), @XmlElement(name = "date"), @XmlElement(name = "float"), @XmlElement(
            name = "long"), @XmlElement(name = "int")})
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
                return !name.equals(sf.getFieldName());
            }

            @Override
            public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
            {
                return !name.equals(sf.getFieldName());
            }
        }, null);
    }

    /**
     * Find a field of any type by fusion name.
     *
     * @param fusionName fusion name of the field
     * @return null or an instance of {@link SolrField}
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
     * Get a document's field value by a search server name. Note: An instance of class Term contains the original and
     * mapped value.
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
     * Get a document's field value by a fusion name. Note: An instance of class Term contains the original and mapped
     * value.
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
        return addField(name, Arrays.asList(value));
    }

    public SolrField addField(String name, List<String> value)
    {
        SolrField result = null;
        if (value.size() == 1)
        {
            SolrSingleValuedField f = new SolrSingleValuedField();
            f.setFieldName(name);
            f.setValue(value.get(0));
            f.setTerm(Term.newSearchServerTerm(f.getFieldName(), f.getValue()));
            addSingleField(f);
            result = f;
        }
        else if (value.size() > 1)
        {
            SolrMultiValuedField f = new SolrMultiValuedField();
            f.setFieldName(name);
            List<String> vals = new ArrayList<>();
            vals.addAll(value);
            f.setValues(vals);
            f.setTerm(Term.newSearchServerTerm(f.getFieldName(), f.getValues()));
            addMultiField(f);
            result = f;
        }
        else
        {
            throw new RuntimeException("No value given for field: '" + name + "'");
        }
        return result;
    }

    protected void addMultiField(SolrMultiValuedField f)
    {
        if (solrMultiValuedFields == null)
        {
            solrMultiValuedFields = new ArrayList<>();
        }
        solrMultiValuedFields.add(f);
    }

    public SolrField addFusionField(String fusionName, FusionField fusionField, List<String> value,
        List<Integer> wordCount)
    {
        Term term = Term.newFusionTerm(fusionName, value);
        term.setFusionField(fusionField);
        term.setFusionFacetCount(wordCount);
        return wrapFusionTermWithSolrField(term, fusionField);
    }

    public SolrField wrapFusionTermWithSolrField(Term term, FusionField fusionField)
    {
        SolrField result = null;
        if (fusionField.isSingleValue())
        {
            if (term.getFusionFieldValue().size() != 1)
            {
                log.warn("Got invalid number of values ({} != 1) for solr single field {}",
                    term.getFusionFieldValue().size(), term.getFusionFieldName());
            }
            SolrSingleValuedField f = new SolrSingleValuedField();
            f.setTerm(term);
            addSingleField(f);
            result = f;
        }
        else
        {
            SolrMultiValuedField f = new SolrMultiValuedField();
            f.setTerm(term);
            addMultiField(f);
            result = f;
        }
        return result;

    }

    protected void addSingleField(SolrSingleValuedField f)
    {
        if (solrSingleValuedFields == null)
        {
            solrSingleValuedFields = new ArrayList<>();
        }
        solrSingleValuedFields.add(f);
    }

    public void addUnsetFusionFieldsOf(Document toMerge, IdGeneratorIfc idHandler)
    {
        List<SolrSingleValuedField> solrSingleValuedFieldsToMerge = toMerge.getSolrSingleValuedFields();
        if (solrSingleValuedFieldsToMerge != null)
        {
            for (SolrSingleValuedField singleField : solrSingleValuedFieldsToMerge)
            {
                if (singleField.getTerm().isWasMapped() && !singleField.getTerm().isRemoved())
                {
                    String fusionFieldName = singleField.getTerm().getFusionFieldName();
                    SolrField sf = findFieldByFusionName(fusionFieldName);
                    if (sf == null)
                    {
                        addSingleField(singleField);
                    }
                }
            }
        }

        List<SolrMultiValuedField> solrMultiValuedFieldsToMerge = toMerge.getSolrMultiValuedFields();
        if (solrMultiValuedFieldsToMerge != null)
        {
            for (SolrMultiValuedField multiField : solrMultiValuedFieldsToMerge)
            {
                if (multiField.getTerm().isWasMapped() && !multiField.getTerm().isRemoved())
                {
                    SolrField sf = findFieldByFusionName(multiField.getTerm().getFusionFieldName());
                    if (sf == null)
                    {
                        addMultiField(multiField);
                    }
                }
            }
        }

        // always merge ids, so that subsequent queries can request the complete set of fields
        mergeDocIds(toMerge, idHandler);
    }


    protected void mergeDocIds(Document toMerge, IdGeneratorIfc idHandler)
    {
        String fusionIdField = idHandler.getFusionIdField();
        String thisId = getFusionDocId(fusionIdField);
        String otherId = toMerge.getFusionDocId(fusionIdField);
        if (thisId != null && otherId != null)
        {
            String mergedThisIdStr = idHandler.mergeIds(thisId, otherId);
            setFusionDocId(fusionIdField, mergedThisIdStr);
        }
        else
        {
            if (log.isWarnEnabled())
            {
                log.warn("Can't merge document ids, because either this document ({}) or the document to merge ({}) " +
                        "contains no fusion id. The documents are merged nevertheless on the merge field.",
                    buildFusionDocStr(), toMerge.buildFusionDocStr());
            }
        }
    }

    public String buildFusionDocStr()
    {
        final StringBuilder sb = new StringBuilder();
        accept(new FieldVisitor()
        {
            protected void add(boolean isMultiple, Term t)
            {
                if (!t.isWasMapped())
                {
                    sb.append("UNMAP: ");
                    sb.append(t.getSearchServerFieldName());
                    if (isMultiple)
                    {
                        List<String> searchServerFieldValue = t.getSearchServerFieldValue();
                        if (searchServerFieldValue != null)
                        {
                            sb.append("[" + searchServerFieldValue.size() + "]");
                        }
                    }
                    sb.append("=");
                    sb.append(t.mergeSearchServerValues());
                    sb.append("\n");
                }
                else
                {
                    if (!t.isRemoved())
                    {
                        sb.append(t.getFusionFieldName());
                        if (isMultiple)
                        {
                            List<String> fusionFieldValue = t.getFusionFieldValue();
                            if (fusionFieldValue != null)
                            {
                                sb.append("[" + fusionFieldValue.size() + "]");
                            }
                        }
                        sb.append("=");
                        sb.append(t.mergeFusionValues());
                        sb.append("\n");
                    }
                }
            }

            @Override public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
            {
                add(false, sf.getTerm());
                return true;
            }

            @Override public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
            {
                add(true, msf.getTerm());
                return true;
            }
        }, new ScriptEnv());
        return sb.toString();
    }

    public String getFusionDocId(String fusionIdField)
    {
        String fusionDocId = null;
        Term idTerm = getFieldTermByFusionName(fusionIdField);
        if (idTerm != null)
        {
            // id is always a single value
            fusionDocId = idTerm.getFusionFieldValue().get(0);
        }
        return fusionDocId;
    }

    public void setFusionDocId(String fusionIdField, String fusionDocId)
    {
        Term idTerm = getFieldTermByFusionName(fusionIdField);
        if (idTerm != null)
        {
            // id is always a single value
            idTerm.getFusionFieldValue().set(0, fusionDocId);
        }
    }

    public String getMergedFusionValue(String field)
    {
        String mergedValue = null;

        Term mergeFieldValueTerm = getFieldTermByFusionName(field);
        if (mergeFieldValueTerm != null)
        {
            mergedValue = mergeFieldValueTerm.mergeFusionValues();
        }
        return mergedValue;
    }

    public List<String> getFusionValuesOf(String field)
    {
        List<String> result = null;

        Term fieldValueTerm = getFieldTermByFusionName(field);
        if (fieldValueTerm != null)
        {
            result = fieldValueTerm.getFusionFieldValue();
        }
        return result;
    }

    /**
     * Replaces the values of an already added fusion field.
     *
     * @param field
     * @param values
     */
    public void replaceFusionValuesOf(String field, List<String> values)
    {
        SolrField solrField = findFieldByFusionName(field);
        solrField.getTerm().setFusionFieldValue(values);
    }

    public List<String> getSearchServerValuesOf(String field)
    {
        List<String> result = null;

        Term fieldValueTerm = getFieldTermByName(field);
        if (fieldValueTerm != null)
        {
            result = fieldValueTerm.getSearchServerFieldValue();
        }
        return result;
    }

    public String getSearchServerDocId(String searchServerIdField)
    {
        String docId = null;
        Term idTerm = getFieldTermByName(searchServerIdField);
        if (idTerm != null)
        {
            // id is always a single value
            docId = idTerm.getSearchServerFieldValue().get(0);
        }
        return docId;
    }

    public void setSearchServerDocId(String searchServerIdField, String searchServerDocId)
    {
        Term idTerm = getFieldTermByName(searchServerIdField);
        if (idTerm == null)
        {
            SolrSingleValuedField sf = new SolrSingleValuedField();
            sf.setFieldName(searchServerIdField);
            sf.setValue(searchServerDocId);
            sf.afterUnmarshal(null, null);
            addSingleField(sf);
        }
        else
        {
            // id is always a single value
            idTerm.getSearchServerFieldValue().set(0, searchServerDocId);
        }
    }

}