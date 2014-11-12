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
import org.outermedia.solrfusion.mapper.MapOperation;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.outermedia.solrfusion.types.TypeResult;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract data holder class to store target configurations (&lt;query&gt;, &lt;response&gt;, &lt;query-response&gt;).
 *
 * @author ballmann
 */

@XmlTransient
@Getter
@Setter
@ToString(exclude = { "op" })
public abstract class Operation
{
    @XmlElements(value = {
        @XmlElement(name = "query", type = Query.class,
            namespace = "http://solrfusion.outermedia.org/configuration/"), @XmlElement(name = "response",
        type = Response.class,
        namespace = "http://solrfusion.outermedia.org/configuration/"), @XmlElement(name = "query-response",
        type = QueryResponse.class,
        namespace = "http://solrfusion.outermedia.org/configuration/")
    })
    private List<Target> targets;

    @XmlTransient
    private MapOperation op;

    /**
     * Get all targets which are applicable to a query. Filters out Response targets.
     *
     * @param target
     * @return a list of target object, perhaps empty. The objects are either of class Query or QueryResponse.
     */
    protected List<Target> getQueryTargets(QueryTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof QueryResponse)
                {
                    result.add(t);
                }
                if (t instanceof Query)
                {
                    if (target.matches(((Query) t).getTarget()))
                    {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all targets which are applicable to a query.
     *
     * @return a list of target object, perhaps empty. The objects are of class Query.
     */
    protected List<Query> getQueryOnlyTargets(QueryTarget target)
    {
        List<Query> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Query)
                {
                    if (target.matches(((Query) t).getTarget()))
                    {
                        result.add((Query) t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all targets which are applicable to a response. Filters out Query targets.
     *
     * @return a list of target object, perhaps empty. The objects are either of class Response or QueryResponse.
     */
    protected List<Target> getResponseTargets(ResponseTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof QueryResponse)
                {
                    result.add(t);
                }
                if (t instanceof Response)
                {
                    ResponseTarget ruleTarget = ((Response) t).getTarget();
                    if (target.matches(ruleTarget))
                    {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all targets which are applicable to a response.
     *
     * @return a list of target object, perhaps empty. The objects are of class Response.
     */
    protected List<Response> getResponseOnlyTargets(ResponseTarget target)
    {
        List<Response> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Response)
                {
                    if (target.matches(((Response) t).getTarget()))
                    {
                        result.add((Response) t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all targets which are applicable to a query and response.
     *
     * @return a list of target object, perhaps empty. The objects are of class Response.
     */
    protected List<QueryResponse> getQueryResponseOnlyTargets()
    {
        List<QueryResponse> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof QueryResponse)
                {
                    result.add((QueryResponse) t);
                }
            }
        }
        return result;
    }

    public void applyAllQueryOperations(Term term, ScriptEnv env, QueryTarget target, int lineNumber)
    {
        ScriptEnv newEnv = getQueryScriptEnv(term, env);
        List<Target> queryTargets = getQueryTargets(target);
        for (Target t : queryTargets)
        {
            applyOneQueryOperation(term, newEnv, t);
        }
    }

    public ScriptEnv getQueryScriptEnv(Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_IN_CONVERSION, ConversionDirection.FUSION_TO_SEARCH);
        return newEnv;
    }

    protected void applyOneQueryOperation(Term term, ScriptEnv newEnv, Target t)
    {
        // the searchServerFieldValue is initialized with the fusionFieldValue
        // because it is possible to apply several mappings in sequence the searchServerFieldValue
        // has to be used
        TypeResult opResult = t.apply(term.getSearchServerFieldValue(), term.getSearchServerFacetCount(), newEnv,
            ConversionDirection.FUSION_TO_SEARCH);
        if (opResult != null)
        {
            term.setSearchServerFieldValue(opResult.getValues());
            term.setSearchServerFacetCount(opResult.getDocCounts());
        }
    }

    public void applyAllResponseOperations(Term term, ScriptEnv env, ResponseTarget target, int lineNumber)
    {
        ScriptEnv newEnv = getResponseScriptEnv(null, null, term, env);
        List<Target> queryTargets = getResponseTargets(target);
        for (Target t : queryTargets)
        {
            applyOneResponseOperation(term, newEnv, t);
        }
    }

    public ScriptEnv getResponseScriptEnv(String fusionFieldName, FusionField fusionField, Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD, fusionFieldName);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_IN_CONVERSION, ConversionDirection.SEARCH_TO_FUSION);
        newEnv.setBinding(ScriptEnv.ENV_IN_DOC_TERM, term);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION, fusionField);
        return newEnv;
    }

    protected void applyOneResponseOperation(Term term, ScriptEnv newEnv, Target t)
    {
        // the fusionFieldValue is initialized with the searchServerFieldValue
        // because it is possible to apply several mappings in sequence the fusionFieldValue
        // has to be used
        TypeResult opResult = t.apply(term.getFusionFieldValue(), term.getFusionFacetCount(), newEnv,
            ConversionDirection.SEARCH_TO_FUSION);
        if (opResult != null)
        {
            term.setFusionFieldValue(opResult.getValues());
            term.setFusionFacetCount(opResult.getDocCounts());
        }
    }

    /**
     * This method is called in {@link org.outermedia.solrfusion.configuration.FieldMapping#afterUnmarshal(javax.xml.bind.Unmarshaller,
     * Object)} in order to run checks which can't be done by the XML schema validation. Implementations should throw an
     * UnmarshalException in the case of errors.
     */
    protected abstract void check(FieldMapping fieldMapping) throws UnmarshalException;
}
