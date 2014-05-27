package org.outermedia.solrfusion.types;

import java.util.List;

import javax.xml.bind.Element;

import lombok.ToString;

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
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static Js getInstance()
	{
		return new Js();
	}
}
