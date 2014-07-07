package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;

/**
 * Parses a solr server's xml response into an internal representation.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", propOrder = { "header", "error", "result"})
@XmlRootElement(name = "response")
@ToString
@Getter
@Setter
public class XmlResponse
{
    @XmlElement(name = "lst", required = true)
    private Object header;

    @XmlElement(name = "lst", required = false)
    private ResponseError error;

    @XmlElement(name = "result", required = true)
    private Result result;


}
