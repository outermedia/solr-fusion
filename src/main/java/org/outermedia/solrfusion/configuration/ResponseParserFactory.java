package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data holder class to store the response parser factory's class.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseParser", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseParserFactory extends ConfiguredFactory
{}
