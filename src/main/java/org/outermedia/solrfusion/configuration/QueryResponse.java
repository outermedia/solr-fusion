package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class to store one "query-response".
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryResponse", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"nodes"
})
@Getter
@Setter
@ToString(callSuper = true)
public class QueryResponse extends Target
{}
