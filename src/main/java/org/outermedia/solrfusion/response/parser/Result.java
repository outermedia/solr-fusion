package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Data holder class keeping the a solr result object.
 *
 * @author stephan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultType")
@Getter
@Setter
@ToString
public class Result {

    @XmlAttribute(name = "name", required = true)
    private String resultName;

    @XmlAttribute(name = "numFound", required = true)
    private int numFound;

    @XmlAttribute(name = "start", required = true)
    private int start;

    @XmlAttribute(name = "maxScore", required = true)
    private float maxScore;

    @XmlElement(name = "doc", required = true)
    @Getter
    @Setter
    private List<Document> documents;

}
