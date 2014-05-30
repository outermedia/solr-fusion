package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.w3c.dom.Element;

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
	public void passArguments(List<Element> typeConfig)
	{
		// TODO Auto-generated method stub

	}

	public static TableFile getInstance()
	{
		return new TableFile();
	}
}
