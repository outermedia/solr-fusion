package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * A Bean Shell interpreter which evaluates expressions contained in the xml to process a field conversion.
 *
 * @author ballmann
 */

@Slf4j
@ToString(callSuper = true, exclude = {"engine", "engineName"})
@Getter
@Setter
public class Bsh extends AbstractType
{

    private String code;

    // either beanshell or bsh
    private String engineName = "beanshell";

    private ScriptEngine engine;

    protected Bsh()
    {
        engine = getScriptEngine(engineName);
    }

    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        String xpathStr = "//:script";
        try
        {
            code = util.getValueOfXpath(xpathStr, typeConfig);
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: "
                    + typeConfig, e);
        }
    }

    /**
     * Returns either null or a String of the evaluated expression's result.
     *
     *
     * @param values
     * @param env
     * @return
     */
    @Override
    public List<String> apply(List<String> values, ScriptEnv env)
    {
        return applyScriptEngineCode(engine, code, values, env);
    }

    public static Bsh getInstance()
    {
        return new Bsh();
    }

}
