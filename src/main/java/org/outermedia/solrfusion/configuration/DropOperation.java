package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder to store drop operations.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dropOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"targets"
})
@Getter
@Setter
@ToString(callSuper = true)
public class DropOperation extends Operation
{}
