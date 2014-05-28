package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.w3c.dom.Element;

@ToString
public class DummyType extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig)
	{
		//  NOP
	}

	public static DummyType getInstance()
	{
		return new DummyType();
	}
}
