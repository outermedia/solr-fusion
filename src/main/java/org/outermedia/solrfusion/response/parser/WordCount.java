package org.outermedia.solrfusion.response.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.*;

/**
 * Created by ballmann on 8/11/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseFacetWordCount")
@ToString
@Slf4j
@Getter
@Setter
public class WordCount
{
    @XmlAttribute(name = "name", required = true)
    private String word;

    @XmlValue
    private int count;
}
