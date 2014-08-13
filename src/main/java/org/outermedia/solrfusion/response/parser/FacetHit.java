package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 8/11/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseFacetField")
@ToString
@Slf4j
@Getter
@Setter
public class FacetHit
{
    @XmlAttribute(name = "name", required = true)
    private String searchServerFieldName;

    @XmlElement(name = "int", required = true)
    private List<WordCount> fieldCounts;

    @XmlTransient
    private Document document;

    public void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        document = new Document();
        if(fieldCounts != null)
        {
            SolrMultiValuedField multiValuedField = new SolrMultiValuedField();
            List<String> values = new ArrayList<>();
            List<Integer> wordCounts = new ArrayList<>();
            for(WordCount wc : fieldCounts)
            {
                values.add(wc.getWord());
                wordCounts.add(wc.getCount());
            }
            multiValuedField.setFieldName(searchServerFieldName);
            multiValuedField.setValues(values);
            multiValuedField.afterUnmarshal(null, null);
            multiValuedField.getTerm().setSearchServerFacetCount(wordCounts);
            document.addMultiField(multiValuedField);
        }
    }

    public Document getDocument(String searchServerIdField, int docId)
    {
        document.setSearchServerDocId(searchServerIdField, String.valueOf(docId));
        return document;
    }
}
