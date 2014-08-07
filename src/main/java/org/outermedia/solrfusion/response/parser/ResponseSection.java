package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 7/7/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseSection")
@ToString(exclude = {"lists"})
@Slf4j
@XmlSeeAlso(Highlighting.class)
public class ResponseSection
{
    @XmlElements(value = {@XmlElement(name = "str"), @XmlElement(name = "int")})
    private List<SolrSingleValuedField> sections;

    @XmlAttribute(name = "name") @Getter
    private String name;

    @XmlAnyElement
    private List<Element> lists;

    @Getter
    @XmlTransient
    private List<Highlighting> highlighting;

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
        if (sections != null)
        {
            for (SolrSingleValuedField sf : sections)
            {
                if (sf.getFieldName().equals(fieldName))
                {
                    result = sf.getValue();
                    break;
                }
            }
        }
        return result;
    }

    protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        if ("facet_counts".equals(name))
        {
            // TODO parse xml
        }

        if ("highlighting".equals(name) && lists != null)
        {
            highlighting = new ArrayList<>();
            Util xmlUtil = new Util();
            for (Element node : lists)
            {
                try
                {
                    // System.out.println("PARSE\n" + );
                    Highlighting hl = xmlUtil.unmarshal(Highlighting.class, node);
                    if (hl != null)
                    {
                        highlighting.add(hl);
                    }
                }
                catch (Exception e)
                {
                    String xmlStr = "???";
                    try
                    {
                        xmlStr = xmlUtil.xmlToString(node);
                    }
                    catch (TransformerException e1)
                    {
                        // NOP
                    }
                    log.error("Caught excpetion while parsing highlighting {}", xmlStr, e);
                }
            }
        }
    }
}
