package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ballmann on 8/5/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "highlighting")
@ToString
@Slf4j
@Getter
@Setter
public class Highlighting
{
    @XmlAttribute(name = "name", required = true)
    private String docId;

    @XmlElement(name = "arr", required = true)
    private List<SolrMultiValuedField> highlightedTexts;

    @XmlTransient
    private Document doc;

    protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        if(highlightedTexts != null && highlightedTexts.size() > 0)
        {
            doc = new Document();
            doc.setSolrMultiValuedFields(highlightedTexts);
        }
    }

    public Document getDocument(String searchServerIdField)
    {
        Document result = doc;
        if(result == null)
        {
            result = new Document();
        }
        result.setSearchServerDocId(searchServerIdField, docId);
        return result;
    }
}
