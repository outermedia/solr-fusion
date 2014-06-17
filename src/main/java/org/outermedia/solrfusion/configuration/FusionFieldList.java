package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ballmann on 6/17/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fusionSchemaFields", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "fusionFields"
        })
@Getter
@Setter
@ToString
public class FusionFieldList
{
    @XmlAttribute(name = "default-type", required = true)
    private String defaultType;

    @XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<FusionField> fusionFields;
}
