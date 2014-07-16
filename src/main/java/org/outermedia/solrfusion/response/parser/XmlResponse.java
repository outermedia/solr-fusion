package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Parses a solr server's xml response into an internal representation.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"header", "result"})
@XmlRootElement(name = "response")
@ToString
public class XmlResponse
{
    @XmlElement(name = "lst", required = true)
    private List<ResponseSection> header;

    @XmlElement(name = "result", required = true)
    private Result result;

    @XmlTransient
    @Getter
    @Setter
    private Exception errorReason;

    public ResponseSection getResponseHeader()
    {
        return findSectionByName("responseHeader");
    }

    public ResponseSection getResponseErrors()
    {
        return findSectionByName("error");
    }

    protected ResponseSection findSectionByName(String n)
    {
        ResponseSection result = null;
        if (header != null)
        {
            for (ResponseSection re : header)
            {
                if (n.equals(re.getName()))
                {
                    result = re;
                    break;
                }
            }
        }
        return result;
    }

    public List<Document> getDocuments()
    {
        List<Document> docs = null;
        if (result != null)
        {
            docs = result.getDocuments();
        }
        return docs;
    }

    public int getNumFound()
    {
        int nr = 0;
        if(result != null)
        {
            nr = result.getNumFound();
        }
        return nr;
    }

    public String getResultName()
    {
        String n = null;
        if(result != null)
        {
            n = result.getResultName();
        }
        return n;
    }
}
