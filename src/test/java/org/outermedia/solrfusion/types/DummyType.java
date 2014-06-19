package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

@ToString
public class DummyType extends AbstractType
{

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		//  NOP
	}

    @Override
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        return null;
    }

    public static DummyType getInstance()
	{
		return new DummyType();
	}
}
