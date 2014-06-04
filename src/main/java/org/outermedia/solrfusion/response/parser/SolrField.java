package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;

/**
 * Data holder class keeping the field information of a solr response document.
 *
 * @author stephan
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldType")
@Getter
@Setter
@ToString
public class SolrField {

    @XmlAttribute(name = "name", required = true)
    private String fieldName;

    @XmlValue
    private String value;
}
