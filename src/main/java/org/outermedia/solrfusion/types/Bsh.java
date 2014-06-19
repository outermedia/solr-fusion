package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.script.*;
import java.util.ArrayList;
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

    public Bsh()
    {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName(engineName);
        if (engine == null)
        {
            ScriptEngineManager sem = new ScriptEngineManager();
            List<ScriptEngineFactory> factories = sem.getEngineFactories();
            List<String> knownEngineNames = new ArrayList<>();
            for (javax.script.ScriptEngineFactory f : factories)
            {
                knownEngineNames.add(f.getNames().toString());
            }

            log.error("Didn't find engine for '" + engineName + "'. Known are: " + knownEngineNames);
        }
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
     * @param env
     * @return
     */
    @Override
    public List<String> apply(ScriptEnv env)
    {
        Bindings bindings = engine.createBindings();
        bindings.putAll(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
        env.flatten(bindings);
        Object evaluated = null;
        try
        {
            evaluated = engine.eval(code, bindings);
        }
        catch (ScriptException e)
        {
            log.error("Caught exception while evaluating bsh code: {}", code, e);
        }
        List<String> result = null;
        if (evaluated != null)
        {
            result = new ArrayList<>();
            if (evaluated instanceof List)
            {
                result.addAll((java.util.Collection<? extends String>) evaluated);
            }
            else
            {
                result.add(evaluated.toString());
            }
        }
        return result;
    }

    public static Bsh getInstance()
    {
        return new Bsh();
    }

}
