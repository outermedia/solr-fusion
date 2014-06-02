package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

/**
 * A Javascript Shell interpreter which evaluates expressions contained in the
 * xml to process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class Js extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		// TODO Auto-generated method stub

	}

	public static Js getInstance()
	{
		return new Js();
	}
}
