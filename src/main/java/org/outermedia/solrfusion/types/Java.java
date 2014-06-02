package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

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

	public static Java getInstance()
	{
		return new Java();
	}
}
