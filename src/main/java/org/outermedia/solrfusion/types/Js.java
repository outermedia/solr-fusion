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
 * A Javascript Shell interpreter which evaluates expressions contained in the
 * xml to process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true, exclude = {"engine", "engineName"})
@Getter
@Setter
@Slf4j
public class Js extends AbstractType
{

    // either js, rhino, JavaScript, javascript, ECMAScript or ecmascript
    private String engineName = "js";

    private ScriptEngine engine;

    private String code;

    protected Js() {
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
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
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
                    + elementListToString(typeConfig), e);
        }
        logBadConfiguration(code != null, typeConfig);
	}

    @Override
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        return applyScriptEngineCode(engine, code, values, env);
    }

    public static Js getInstance()
	{
		return new Js();
	}
}
