package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A regular expression which matches a pattern and applies a replacement in
 * order to process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class RegularExpression extends AbstractType
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

    public static RegularExpression getInstance()
	{
		return new RegularExpression();
	}
}
