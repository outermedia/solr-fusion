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

    /**
     * The expected configuration is:
     * <pre>
     * {@code<script>
     *      // ... your code ...
     *  </script>
     * }
     * </pre>
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        code = getConfigString("script", typeConfig, util);
        logBadConfiguration(code != null, typeConfig);
    }

    /**
     * Returns either null or a String of the evaluated expression's result.
     *
     * @param values
     * @param env
     * @param dir
     * @return
     */
    @Override
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        return applyScriptEngineCode(engine, code, values, env);
    }

    public static Bsh getInstance()
    {
        return new Bsh();
    }

}
