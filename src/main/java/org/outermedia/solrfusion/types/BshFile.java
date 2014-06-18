package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A Bean Shell interpreter which evaluates expressions contained in a file to
 * process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class BshFile extends AbstractType
{

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

    public static BshFile getInstance()
	{
		return new BshFile();
	}
}
