package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * This class enumerates all supported targets for query specific mapping rules.
 *
 * Created by ballmann on 7/21/14.
 */
@XmlType
@XmlEnum(String.class)
public enum QueryTarget
{
    @XmlEnumValue("all") ALL,
    @XmlEnumValue("filter-query") FILTER_QUERY,
    @XmlEnumValue("query") QUERY,
    @XmlEnumValue("highlight-query") HIGHLIGHT_QUERY;

    public boolean matches(QueryTarget specificTarget)
    {
        // null means ALL
        if(this == ALL || specificTarget == null) return true;
        return this == specificTarget;
    }
}
