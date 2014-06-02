package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

/**
 * A Bean Shell interpreter which evaluates expressions contained in the xml to
 * process a field conversion.
 * 
 * @author ballmann
 * 
 */

@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
public class Bsh extends AbstractType
{

	private String code;

	@Override
	public void passArguments(List<Element> typeConfig, Util util)
	{
		/* unfortunately the ":" is necessary for the empty xml namespace!
		 * please see Util.getValueOfXpath() */
		String xpathStr = "//:script";
		try
		{
			code = util.getValueOfXpath(xpathStr, typeConfig);
		}
		catch (Exception e)
		{
			log.error("Caught exception while parsing configuration: "
				+ typeConfig, e);
		}
	}

	public static Bsh getInstance()
	{
		return new Bsh();
	}
}
