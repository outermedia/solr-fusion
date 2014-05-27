package org.outermedia.solrfusion.types;

import java.util.List;

import javax.xml.bind.Element;

import lombok.ToString;

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
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static RegularExpression getInstance()
	{
		return new RegularExpression();
	}
}
