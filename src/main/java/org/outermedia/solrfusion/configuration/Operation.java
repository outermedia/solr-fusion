package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract data holder class to store targets (&lt;query&gt;, &lt;response&gt;,
 * &lt;query-response&gt;).
 * 
 * @author ballmann
 * 
 */

@XmlTransient
@Getter
@Setter
@ToString
public abstract class Operation
{
	@XmlElements(value =
	{
		@XmlElement(name = "query", type = Query.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
		@XmlElement(name = "response", type = Response.class, namespace = "http://solrfusion.outermedia.org/configuration/"),
		@XmlElement(name = "query-response", type = QueryResponse.class, namespace = "http://solrfusion.outermedia.org/configuration/")
	})
	private List<Target> targets;
}
