package org.outermedia.solrfusion.configuration;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
