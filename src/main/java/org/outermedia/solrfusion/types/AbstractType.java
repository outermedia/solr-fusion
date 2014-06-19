package org.outermedia.solrfusion.types;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ScriptType;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.script.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract parent class of all "types" which are referenced in &lt;query&gt;, &lt;response&gt; and
 * &lt;query-response&gt; and declared in &lt;script-type&gt;.
 *
 * @author ballmann
 */

@ToString
@Slf4j
public abstract class AbstractType implements Initiable<ScriptType>
{

    /**
     * Pass global configuration to the instance.
     */
    public void init(ScriptType config)
    {
        // NOP
    }

    /**
     * Pass the configuration when the type is used.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    public abstract void passArguments(List<Element> typeConfig, Util util);

    /**
     * This method applies 'this' script type to a given value (contained in 'env'). Available env entries described in
     * {@link org.outermedia.solrfusion.types.ScriptEnv}
     *
     * @param values
     * @param env
     * @return perhaps null
     */
    public abstract List<String> apply(List<String> values, ScriptEnv env);

    public ScriptEngine getScriptEngine(String engineName)
    {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(engineName);
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
        return engine;
    }

    public List<String> applyScriptEngineCode(ScriptEngine engine, String code, List<String> values, ScriptEnv env)
    {
        Bindings bindings = engine.createBindings();
        bindings.putAll(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_VALUES, values);
        newEnv.flatten(bindings);
        Object evaluated = null;
        try
        {
            evaluated = engine.eval(code, bindings);
        }
        catch (ScriptException e)
        {
            log.error("Caught exception while evaluating code: {}", code, e);
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
}
