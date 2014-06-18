package org.outermedia.solrfusion.types;

import lombok.ToString;
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
public class Js extends AbstractType
{

    // either js, rhino, JavaScript, javascript, ECMAScript or ecmascript
    private String engineName = "js";

    private ScriptEngine engine;

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		// TODO Auto-generated method stub

	}

    @Override
    public List<String> apply(ScriptEnv env)
    {
        return null; // TODO
    }

    public static Js getInstance()
	{
		return new Js();
	}
}
