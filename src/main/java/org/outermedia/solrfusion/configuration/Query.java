package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class to store one "query".
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "query", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"nodes"
})
@Getter
@Setter
@ToString(callSuper = true)
public class Query extends Target
{}
