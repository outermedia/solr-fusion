package org.outermedia.solrfusion.types;

import java.util.List;

import lombok.ToString;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ScriptType;
import org.w3c.dom.Element;

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
	 */
	public abstract void passArguments(List<Element> typeConfig);

}
