package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A mapping table, contained in a file, is used to replace field values.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class TableFile extends AbstractType
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

    public static TableFile getInstance()
	{
		return new TableFile();
	}
}
