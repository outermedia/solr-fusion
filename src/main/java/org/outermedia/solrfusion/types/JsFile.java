package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A Javascript Shell interpreter which evaluates expressions contained in a
 * file to process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class JsFile extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		// TODO Auto-generated method stub

	}

    @Override
    public String apply(ScriptEnv env)
    {
        return null; // TODO
    }

    public static JsFile getInstance()
	{
		return new JsFile();
	}
}
