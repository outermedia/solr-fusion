package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.Element;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract data holder class to store the common attributes of a &lt;query&gt;,
 * &lt;response&gt; or &lt;query-response&gt;.
 * 
 * @author ballmann
 * 
 */

@XmlTransient
@Getter
@Setter
@ToString
public abstract class Target
{
	@XmlIDREF
	@XmlAttribute(name = "type", required = true)
	private ScriptType type;

	@XmlAttribute(name = "name", required = false)
	private String name;

	@XmlAttribute(name = "fusion-name", required = false)
	private String fusionName;

	@XmlAnyElement
	private List<Element> nodes;
}
