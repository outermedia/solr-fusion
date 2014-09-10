package org.outermedia.solrfusion.response.freemarker;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.mapper.Term;
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

    @Getter @Setter private String id;

    protected boolean forceMultiValue;

    public FreemarkerDocument()
    {
        this(false);
    }

    public FreemarkerDocument(boolean forceMultiValue)
    {
        this.forceMultiValue = forceMultiValue;
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

        if (forceMultiValue || field.isMultiValue())
        {
            // fusion-field is configured as multivalue, but solr server gave a single valued field
            FreemarkerMultiValuedField freemarkerField = FreemarkerMultiValuedField.fromSolrField(sf);
            addMultiValuedField(freemarkerField);
            // log.debug("MULTI FIELD {}: {}", sf.getTerm().getFusionFieldName(), sf);
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

        Term t = null;
        List<String> values = null;
        if(sf != null) t = sf.getTerm();
        if(t != null && t.isWasMapped() && !t.isRemoved()) values = t.getFusionFieldValue();
        if (!forceMultiValue && field.isSingleValue() && values != null && values.size() > 1)
        {
            // error in mapping. will be logged and nothing is rendered
            log.error("Unable to render multiple values in single valued field {}", field.getFieldName());
            return true;
        }

        if (!forceMultiValue && field.isSingleValue())
        {
            FreemarkerSingleValuedField freemarkerField = FreemarkerSingleValuedField.fromSolrField(sf);
            addSingleValuedField(freemarkerField);
        } else
        {
            FreemarkerMultiValuedField freemarkerField = FreemarkerMultiValuedField.fromSolrField(sf);
            addMultiValuedField(freemarkerField);
        }
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

