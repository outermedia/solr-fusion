package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;

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
@ToString
public abstract class Operation
{
    @XmlElements(value =
            {
                    @XmlElement(name = "query", type = Query.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "response", type = Response.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "query-response", type = QueryResponse.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/")
            })
    private List<Target> targets;

    /**
     * Get all targets which are applicable to a query. Filters out Response targets.
     *
     * @return a list of target object, perhaps empty. The objects are either of class Query or QueryResponse.
     */
    protected List<Target> getQueryTargets()
    {
        List<Target> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Query || t instanceof QueryResponse)
                {
                    result.add(t);
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
    protected List<Query> getQueryOnlyTargets()
    {
        List<Query> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Query)
                {
                    result.add((Query) t);
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
    protected List<Target> getResponseTargets()
    {
        List<Target> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Response || t instanceof QueryResponse)
                {
                    result.add(t);
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
    protected List<Response> getResponseOnlyTargets()
    {
        List<Response> result = new ArrayList<>();
        if (targets != null)
        {
            for (Target t : targets)
            {
                if (t instanceof Response)
                {
                    result.add((Response) t);
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

    public void applyAllQueryOperations(Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_CONVERSION, ConversionDirection.FUSION_TO_SEARCH);
        List<Target> queryTargets = getQueryTargets();
        for (Target t : queryTargets)
        {
            applyOneQueryOperation(term, newEnv, t);
        }
    }

    protected void applyOneQueryOperation(Term term, ScriptEnv newEnv, Target t)
    {
        // the searchServerFieldValue is initialized with the fusionFieldValue
        // because it is possible to apply several mappings in sequence the searchServerFieldValue
        // has to be used
        List<String> newSearchServerValue = t.apply(term.getSearchServerFieldValue(), newEnv,
                ConversionDirection.FUSION_TO_SEARCH);
        term.setSearchServerFieldValue(newSearchServerValue);
    }

    public void applyAllResponseOperations(Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_CONVERSION, ConversionDirection.SEARCH_TO_FUSION);
        List<Target> queryTargets = getResponseTargets();
        for (Target t : queryTargets)
        {
            applyOneResponseOperation(term, newEnv, t);
        }
    }

    protected void applyOneResponseOperation(Term term, ScriptEnv newEnv, Target t)
    {
        // the fusionFieldValue is initialized with the searchServerFieldValue
        // because it is possible to apply several mappings in sequence the fusionFieldValue
        // has to be used
        List<String> newFusionValue = t.apply(term.getFusionFieldValue(), newEnv, ConversionDirection.SEARCH_TO_FUSION);
        term.setFusionFieldValue(newFusionValue);
    }

    /**
     * This method is called in {@link org.outermedia.solrfusion.configuration.FieldMapping#afterUnmarshal(javax.xml.bind.Unmarshaller,
     * Object)} in order to run checks which can't be done by the XML schema validation.
     * Implementations should throw an UnmarshalException in the case of errors.
     */
    protected abstract void check(FieldMapping fieldMapping) throws UnmarshalException;
}
