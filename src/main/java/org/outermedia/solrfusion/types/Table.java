package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.w3c.dom.Element;

/**
 * A mapping table, embedded in the xml, is used to replace field values.
 * 
 * @author ballmann
 * 
 */

@ToString(callSuper = true)
public class Table extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static Table getInstance()
	{
		return new Table();
	}
}
