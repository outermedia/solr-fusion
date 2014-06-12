package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Base Class for SolrSingleValuedField and SolrMultiValuedField to be accessed uniformly.
 *
 * @author stephan
 */

@XmlTransient
public abstract class SolrField {

    @XmlAttribute(name = "name", required = true)
    @Getter
    @Setter
    protected String fieldName;

    public abstract String getFirstSearchServerFieldValue();

    public abstract List<String> getAllSearchServerFieldValue();
}