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

import org.apache.solr.client.solrj.impl.BinaryResponseParser;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The three supported response renderer types:
 * <ul>
 * <li>{@link #XML}</li>
 * <li> {@link #JSON}</li>
 * <li>{@link #PHP}</li>
 * <li>{@link #JAVABIN}</li>
 * </ul>
 *
 * @author ballmann
 */

@XmlType
@XmlEnum(String.class)
public enum ResponseRendererType
{
    @XmlEnumValue("xml")
    XML("text/xml;charset=UTF-8"),
    @XmlEnumValue("json")
    JSON("application/json;charset=UTF-8"),
    @XmlEnumValue("php")
    PHP("text/x-php;charset=UTF-8"),
    @XmlEnumValue("javabin")
    JAVABIN(BinaryResponseParser.BINARY_CONTENT_TYPE + ";charset=UTF-8");

    private String mimeType;

    ResponseRendererType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getMimeType()
    {
        return mimeType;
    }
}