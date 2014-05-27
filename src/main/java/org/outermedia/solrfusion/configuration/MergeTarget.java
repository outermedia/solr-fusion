package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder to store the target part of a merge configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mergeTarget", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString
public class MergeTarget
{
	@XmlAttribute(required = true)
	private int prio;

	@XmlAttribute(name = "target-name", required = true)
	private String targetName;
}
