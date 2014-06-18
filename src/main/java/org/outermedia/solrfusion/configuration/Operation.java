package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract data holder class to store target configurations (&lt;query&gt;,
 * &lt;response&gt;, &lt;query-response&gt;).
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
                    @XmlElement(name = "query", type = Query.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "response", type = Response.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "query-response", type = QueryResponse.class, namespace = "http://solrfusion.outermedia.org/configuration/")
            })
    private List<Target> targets;

    /**
     * Get all targets which are applicable to a query.
     *
     * @return a list of target object, perhaps empty
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
     * Get all targets which are applicable to a response.
     *
     * @return a list of target object, perhaps empty
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

    public void applyAllQueryOperations(Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        List<Target> queryTargets = getQueryTargets();
        for (Target t : queryTargets)
        {
            applyOneQueryOperation(term, newEnv, t);
        }
    }

    protected void applyOneQueryOperation(Term term, ScriptEnv newEnv, Target t)
    {
        List<String> newSearchServerValue = t.apply(newEnv);
        term.setSearchServerFieldValue(newSearchServerValue);
    }

    public void applyAllResponseOperations(Term term, ScriptEnv env)
    {
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_VALUE, term.getFusionFieldValue());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_VALUE, term.getSearchServerFieldValue());
        List<Target> queryTargets = getResponseTargets();
        for (Target t : queryTargets)
        {
            applyOneResponseOperation(term, newEnv, t);
        }
    }

    protected void applyOneResponseOperation(Term term, ScriptEnv newEnv, Target t)
    {
        List<String> newFusionValue = t.apply(newEnv);
        term.setFusionFieldValue(newFusionValue);
    }
}
