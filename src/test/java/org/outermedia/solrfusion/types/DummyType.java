package org.outermedia.solrfusion.types;

import java.util.List;

import javax.xml.bind.Element;

import lombok.ToString;

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
