package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * This class enumerates all supported targets for response specific mapping rules.
 *
 * Created by ballmann on 7/21/14.
 */
@XmlType
@XmlEnum(String.class)
public enum ResponseTarget
{
    @XmlEnumValue("all") ALL,
    @XmlEnumValue("facet") FACET,
    @XmlEnumValue("document") DOCUMENT,
    @XmlEnumValue("highlight") HIGHLIGHT;

    public boolean matches(ResponseTarget specificTarget)
    {
        // null means ALL
        if(this == ALL || specificTarget == null) return true;
        return this == specificTarget;
    }
}
