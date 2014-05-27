package org.outermedia.solrfusion.types;

import java.util.List;

import javax.xml.bind.Element;

import lombok.ToString;

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
