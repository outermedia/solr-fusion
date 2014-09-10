package org.outermedia.solrfusion.configuration;

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
