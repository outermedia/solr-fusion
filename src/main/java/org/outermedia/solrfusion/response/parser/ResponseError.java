package org.outermedia.solrfusion.response.parser;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ballmann on 7/7/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseError")
public class ResponseError
{
    @XmlElements(value =
        {
            @XmlElement(name = "str"),
            @XmlElement(name = "int"),
        })
    private List<SolrSingleValuedField> errorParts;

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
        if(errorParts != null)
        {
            for(SolrSingleValuedField sf : errorParts)
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
