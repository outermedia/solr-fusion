package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by ballmann on 7/21/14.
 */
@XmlType
@XmlEnum(String.class)
public enum AddLevel
{
    @XmlEnumValue("inside") INSIDE,
    @XmlEnumValue("outside") OUTSIDE
}
