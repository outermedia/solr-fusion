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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Data holder class which stores the SolrFusion schema field configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldType", namespace = "http://solrfusion.outermedia.org/configuration/")
@Getter
@Setter
@ToString
public class FusionField
{

    @XmlAttribute(name = "name", required = true)
    private String fieldName;

    @XmlAttribute(name = "type", required = false)
    private String type;

    @XmlAttribute(name = "format", required = false)
    private String format;

    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE) @XmlAttribute(name = "multi-value", required = false)
    protected Boolean multiValue;


    /**
     * Get the {@link #type}'s corresponding enum.
     *
     * @return null for unknown or an instance
     */
    public DefaultFieldType getFieldType()
    {
        DefaultFieldType result = null;
        try
        {
            if (type != null)
            {
                result = DefaultFieldType.valueOf(type.toUpperCase());
            }
        }
        catch (Exception e)
        {
            // NOP
        }
        return result;
    }

    protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        FusionFieldList list = (FusionFieldList) parent;
        if (type == null)
        {
            type = ((FusionFieldList) parent).getDefaultType();
        }
    }

    public boolean isSingleValue()
    {
        return multiValue == null || Boolean.FALSE.equals(multiValue);
    }

    public boolean isMultiValue()
    {
        return Boolean.TRUE.equals(multiValue);
    }

    public void asSingleValue()
    {
        multiValue = null;
    }

    public void asMultiValue()
    {
        multiValue = Boolean.TRUE;
    }
}
