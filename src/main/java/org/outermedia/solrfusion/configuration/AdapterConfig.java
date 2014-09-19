package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by ballmann on 9/18/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adapterConfig", namespace = "http://solrfusion.outermedia.org/configuration/",
    propOrder = {
        "typeConfig"
    })
@Getter
@Setter
@ToString
public class AdapterConfig
{
    @XmlAnyElement
    private List<Element> typeConfig;
}
