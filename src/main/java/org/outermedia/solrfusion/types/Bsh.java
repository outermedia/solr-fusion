package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.w3c.dom.Element;

/**
 * A Bean Shell interpreter which evaluates expressions contained in the xml to
 * process a field conversion.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class Bsh extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static Bsh getInstance()
	{
		return new Bsh();
	}
}
