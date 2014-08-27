package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.*;
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

}
