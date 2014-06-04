package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A given Java class is used to compute a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class Java extends AbstractType
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

    public static Java getInstance()
	{
		return new Java();
	}
}
