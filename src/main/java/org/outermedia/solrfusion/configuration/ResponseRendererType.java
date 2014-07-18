package org.outermedia.solrfusion.configuration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The three supported response renderer types: <li>{@link #XML}</li><li> {@link #JSON}</li><li>{@link #PHP}</li>
 *
 * @author ballmann
 */

@XmlType
@XmlEnum(String.class)
public enum ResponseRendererType
{
    @XmlEnumValue("xml")
    XML("text/xml;charset=UTF-8"), @XmlEnumValue("json")
JSON("application/json;charset=UTF-8"), @XmlEnumValue("php")
PHP("text/x-php;charset=UTF-8");
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