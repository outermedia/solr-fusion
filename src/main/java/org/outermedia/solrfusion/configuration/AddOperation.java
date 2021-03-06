package org.outermedia.solrfusion.configuration;

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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.MapOperation;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.*;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Locale;

/**
 * Data holder to store add operation configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder = {"targets"})
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class AddOperation extends Operation
{
    // responses don't need a level
    @XmlAttribute(required = false)
    private AddLevel level;

    public AddOperation()
    {
        setOp(MapOperation.ADD);
    }

    @Override
    protected boolean applyOneQueryOperation(Term term, ScriptEnv env, Target t)
    {
        boolean result = false;
        // outside added queries will be handled later
        if (level == AddLevel.INSIDE)
        {
            // the super call would overwrite the search server field value which is initialized with the fusion field
            // value either the field is dropped or changed
            // super.applyOneQueryOperation(term, env, t);
            TypeResult opResult = t.apply(term.getFusionFieldValue(), term.getFusionFacetCount(), env,
                ConversionDirection.FUSION_TO_SEARCH);
            if (opResult != null)
            {
                result = opResult.isReturnsFullQueries();
                term.addNewSearchServerQuery(true, opResult.getValues(), env.getConfiguration(), env.getLocale(),
                    !result);
            }
        }
        return result;
    }

    @Override
    protected void applyOneResponseOperation(Term term, ScriptEnv env, Target t)
    {
        // NOP will be done later see addToResponse() below
    }

    public boolean addToResponse(Document doc, String fusionFieldName, FusionField fusionField,
        String searchServerFieldName, Target t, ScriptEnv newEnv)
    {
        boolean added = false;
        Term term = doc.getFieldTermByFusionName(fusionFieldName);
        boolean isNew = false;
        if (term == null)
        {
            // use search server's values as default
            List<String> searchServerValues = null;
            List<Integer> searchServerDocCounts = null;
            if (searchServerFieldName != null)
            {
                Term searchServerTerm = doc.getFieldTermByName(searchServerFieldName);
                if (searchServerTerm != null)
                {
                    searchServerValues = searchServerTerm.getSearchServerFieldValue();
                    searchServerDocCounts = searchServerTerm.getSearchServerFacetCount();
                }
            }
            term = Term.newFusionTerm(fusionFieldName, searchServerValues);
            term.setFusionField(fusionField);
            term.setFusionFacetCount(searchServerDocCounts);
            term.setWasMapped(true);
            isNew = true;
        }
        newEnv.setBinding(ScriptEnv.ENV_IN_DOCUMENT, doc);
        ScriptEnv env = getResponseScriptEnv(fusionFieldName, fusionField, term, newEnv);
        super.applyOneResponseOperation(term, env, t);
        List<String> fusionFieldValue = term.getFusionFieldValue();
        if (isNew && fusionFieldValue != null && fusionFieldValue.size() > 0 &&
            doc.getFieldTermByFusionName(fusionFieldName) == null)
        {
            doc.wrapFusionTermWithSolrField(term, fusionField);
            added = true;
        }
        return added;
    }

    public List<String> addToQuery(Configuration configuration, String searchServerFieldName, Target t, Locale locale)
    {
        Term term = Term.newSearchServerTerm(searchServerFieldName);
        ScriptEnv env = getQueryScriptEnv(term, new ScriptEnv());
        boolean returnsFullQueries = super.applyOneQueryOperation(term, env, t);
        term.addNewSearchServerQuery(false, term.getSearchServerFieldValue(), configuration, locale, !returnsFullQueries);
        List<String> newQueries = term.getNewQueries();
        return newQueries;
    }

    @Override
    public void check(FieldMapping fieldMapping) throws UnmarshalException
    {
        String msg = null;

        // check <om:add><om:response> and <om:add><om:query-response>
        List<Target> responses = getResponseTargets(ResponseTarget.ALL);
        if (responses.size() > 0)
        {
            if (fieldMapping.getFusionName() == null)
            {
                msg = "Please specify a field for attribute 'fusion-name' in order to add something to a response.";
            }
            else
            {
                ScriptType scriptType = new ScriptType();
                scriptType.setClassFactory(CopySearchServerFieldToFusionField.class.getName());
                scriptType.afterUnmarshal(null, null);
                for (Target t : responses)
                {
                    boolean hasSearchServerName = fieldMapping.getSearchServersName() != null;
                    if (!hasSearchServerName && t.getType() == null)
                    {
                        msg = "Please specify a value for the 'type' attribute in the '<om:add>' '<om:response>' operation.";
                    }
                    // set default script type
                    if (hasSearchServerName && t.getType() == null)
                    {
                        t.setType(scriptType);
                    }
                }
            }
        }

        // check <om:add><om:query> and <om:add><om:query-response>
        List<Target> queries = getQueryTargets(QueryTarget.ALL);
        if (queries.size() > 0)
        {
            if (level == null)
            {
                msg = "Please specify the level attribute when <om:add> is used for queries. Possible values are 'inside' and 'outside'.";
            }
            if (level == AddLevel.INSIDE && fieldMapping.getFusionName() == null)
            {
                msg =
                    "Please specify a search server field when you want to add a new query part inside of the current " +
                        "query, because the search server field specifies the place.";
            }
            if (fieldMapping.getSearchServersName() == null)
            {
                msg = "Please specify a field for attribute 'name' in order to add something to a query.";
            }
            else
            {
                ScriptType scriptType = new ScriptType();
                scriptType.setClassFactory(CopyFusionTermQueryToSearchServerQuery.class.getName());
                scriptType.afterUnmarshal(null, null);

                for (Target t : queries)
                {
                    boolean hasFusionName = fieldMapping.getFusionName() != null;
                    if (!hasFusionName && t.getType() == null)
                    {
                        msg = "Please specify a value for the 'type' attribute in the '<om:add>' '<om:query>' operation.";
                    }
                    // set default script type
                    if (hasFusionName && t.getType() == null)
                    {
                        t.setType(scriptType);
                    }
                }
            }
        }

        if (msg != null)
        {
            msg = "In fusion schema at line " + fieldMapping.getStartLineNumberInSchema() + ": " + msg;
            log.error(msg);
            throw new UnmarshalException(msg);
        }
    }
}
