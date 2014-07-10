package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ballmann on 7/7/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseSection")
@ToString
public class ResponseSection
{
    @XmlElements(value =
        {
            @XmlElement(name = "str"),
            @XmlElement(name = "int"),
        })
    private List<SolrSingleValuedField> sections;

    @XmlAttribute(name = "name")
    @Getter
    private String name;

    public String getErrorCode()
    {
        return findFieldValue("code");
    }

    public String getErrorMsg()
    {
        return findFieldValue("msg");
    }


    protected String findFieldValue(String fieldName)
    {
        String result = null;
        if(sections != null)
        {
            for(SolrSingleValuedField sf : sections)
            {
                if(sf.getFieldName().equals(fieldName))
                {
                    result = sf.getValue();
                    break;
                }
            }
        }
        return result;
    }
}
