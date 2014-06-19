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

    @Override
    public List<String> apply(ScriptEnv env)
    {
        return applyScriptEngineCode(engine, code, env);
    }

    public static Js getInstance()
	{
		return new Js();
	}
}
