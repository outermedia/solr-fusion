package org.outermedia.solrfusion.types;

import java.util.List;

import javax.xml.bind.Element;

import lombok.ToString;

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
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static Java getInstance()
	{
		return new Java();
	}
}
