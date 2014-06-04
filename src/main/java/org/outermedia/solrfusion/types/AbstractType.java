package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ScriptType;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Abstract parent class of all "types" which are referenced in &lt;query&gt;,
 * &lt;response&gt; and &lt;query-response&gt; and declared in
 * &lt;script-type&gt;.
 * 
 * @author ballmann
 * 
 */

@ToString
public abstract class AbstractType implements Initiable<ScriptType>
{

	/**
	 * Pass global configuration to the instance.
	 */
	public void init(ScriptType config)
	{
		// NOP
	}

	/**
	 * Pass the configuration when the type is used.
	 * 
	 * @param typeConfig a list of XML elements
	 * @param util helper which simplifies to apply xpaths
	 */
	public abstract void passArguments(List<Element> typeConfig, Util util);

    public abstract String apply(ScriptEnv env);
}
