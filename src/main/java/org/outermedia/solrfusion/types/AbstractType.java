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
     * @param values the values to transform. Not null but perhaps emtpy. Elements of the lists are perhaps null.
     * @param env    the environment contains several predefined values (see {@link org.outermedia.solrfusion.types.ScriptEnv}
     * @param dir    is the conversion direction. Either from fusion schema to search server schema or vice versa.
     * @return perhaps null
     */
    public abstract TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir);

    /**
     * Utility method to get a script engine by name.
     *
     * @param engineName is the name of the engine.
     * @return null if no engine is bound to the specified name. In this case all known names are logged.
     */
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

    /**
     * The code processed by a script engine may return either a String object or a List of String objects. The String
     * is automatically converted into a list with one element.
     *
     * @param engine the engine which shall evaluate the given code and values
     * @param code   the source code to process
     * @param values the values to transform
     * @param env    the environment which is passed to the script engine (see {@link org.outermedia.solrfusion.types.ScriptEnv}
     * @return null in error case
     */
    @SuppressWarnings("unchecked")
    public TypeResult applyScriptEngineCode(ScriptEngine engine, String code, List<String> values,
        List<Integer> facetWordCounts, ScriptEnv env)
    {
        Bindings bindings = engine.createBindings();
        bindings.putAll(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_VALUES, values);
        newEnv.setBinding(ScriptEnv.ENV_IN_WORD_COUNT, facetWordCounts);
        newEnv.setBinding(ScriptEnv.ENV_OUT_NEW_WORD_COUNTS, facetWordCounts);
        newEnv.flatten(bindings);
        Object evaluated = null;
        List<Integer> returnWordCounts = null;
        try
        {
            engine.eval(code, bindings);
            evaluated = bindings.get(ScriptEnv.ENV_OUT_NEW_VALUES);
            returnWordCounts = (List<Integer>) bindings.get(ScriptEnv.ENV_OUT_NEW_WORD_COUNTS);
        }
        catch (ScriptException e)
        {
            log.error("Caught exception while evaluating code: {}", code, e);
        }
        TypeResult result = null;
        List<String> returnValues = null;
        if (evaluated != null)
        {
            returnValues = new ArrayList<>();
            if (evaluated instanceof List)
            {
                returnValues.addAll((java.util.Collection<? extends String>) evaluated);
            }
            else
            {
                returnValues.add(evaluated.toString());
            }
            result = new TypeResult(returnValues, returnWordCounts);
        }
        return result;
    }

    /**
     * If configOk is false, then an error is logged.
     *
     * @param configOk   whether to log a configuration error or not.
     * @param typeConfig is the configuration given to {@link #passArguments(java.util.List,
     *                   org.outermedia.solrfusion.configuration.Util)}
     */
    public void logBadConfiguration(boolean configOk, List<Element> typeConfig)
    {
        if (!configOk)
        {
            log.error("{}: Missing or invalid configuration. Please fix it: {}", getClass().getName(),
                elementListToString(typeConfig));
        }
    }

    public String elementListToString(List<Element> elems)
    {
        StringBuilder sb = new StringBuilder();
        if (elems == null)
        {
            sb.append("null");
        }
        else
        {
            Util util = new Util();
            sb.append("[");
            String se;
            for (int i = 0; i < elems.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                Element n = elems.get(i);
                if (n == null)
                {
                    se = "null";
                }
                else
                {
                    try
                    {
                        se = util.xmlToString(n);
                    }
                    catch (Exception e)
                    {
                        se = n.toString();
                    }
                }
                sb.append(se);
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public String getConfigString(String elementName, List<Element> typeConfig, Util util)
    {
        return getConfigString(elementName, typeConfig, util, true);
    }

    public String getConfigString(String elementName, List<Element> typeConfig, Util util, boolean trim)
    {
        String result = null;
        try
        {
            /* unfortunately the ":" is necessary for the empty xml namespace!
             * please see Util.getValueOfXpath() */
            result = util.getValueOfXpath("//:" + elementName, typeConfig, trim);
        }
        catch (Exception e)
        {
            log.error("Can't get config option '{}'. Caught exception while parsing configuration: {}", elementName,
                elementListToString(typeConfig), e);
        }
        return result;
    }
}
