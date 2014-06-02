package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

@ToString
public class DummyType extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		//  NOP
	}

	public static DummyType getInstance()
	{
		return new DummyType();
	}
}
