package org.outermedia.solrfusion.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.MergeStrategyIfc;

/**
 * Data holder to store the merge configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "merge", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"targets"
})
@Getter
@Setter
@ToString(callSuper = true)
public class Merge extends ConfiguredFactory<MergeStrategyIfc, Merge>
{
	@XmlAttribute(name = "fusion-name", required = true)
	private String fusionName;

	@XmlElement(name = "target", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
	private List<MergeTarget> targets;
}
