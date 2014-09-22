package org.outermedia.solrfusion.configuration;

import com.sun.xml.bind.annotation.XmlLocation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.outermedia.solrfusion.types.TypeResult;
import org.xml.sax.Locator;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 9/22/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "postProcessor", namespace = "http://solrfusion.outermedia.org/configuration/",
    propOrder = {"targets"})
@Getter
@Setter
@ToString(exclude = {"locator"})
@Slf4j
public class PostProcessor
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

    @XmlTransient @XmlLocation
    private Locator locator;

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

    public PostProcessorStatus applyQueryTargets(ScriptEnv env)
    {
        List<Target> queryTargets = getQueryTargets();
        return applyTargetList(queryTargets, env, ConversionDirection.FUSION_TO_SEARCH);
    }

    public PostProcessorStatus applyResponseTargets(ScriptEnv env)
    {
        List<Target> responseTargets = getResponseTargets();
        return applyTargetList(responseTargets, env, ConversionDirection.SEARCH_TO_FUSION);
    }

    protected PostProcessorStatus applyTargetList(List<Target> targets, ScriptEnv env, ConversionDirection dir)
    {
        PostProcessorStatus result = PostProcessorStatus.CONTINUE;
        if (targets != null)
        {
            for (Target t : targets)
            {
                TypeResult r = t.apply(null, null, env, dir);
                if (r != null)
                {
                    List<String> vals = r.getValues();
                    if (vals != null && vals.size() > 0)
                    {
                        PostProcessorStatus status = PostProcessorStatus.valueOf(vals.get(0));
                        if (status == PostProcessorStatus.STOP || status == PostProcessorStatus.DO_NOT_SEND_QUERY)
                        {
                            result = status;
                        }
                        if (!status.doContinue())
                        {
                            break;
                        }
                    }
                }
            }
        }

        if (result == PostProcessorStatus.DO_NOT_SEND_QUERY)
        {
            log.info("Post processor at line {} decided not to send the request to {}.", locator.getLineNumber(),
                env.getSearchServerConfig().getSearchServerName());
        }

        return result;
    }
}
