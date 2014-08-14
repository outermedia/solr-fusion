package org.outermedia.solrfusion.response.parser;

import com.google.common.collect.ComparisonChain;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(exclude = {"sortByCount"})
public class WordCount implements Comparable<WordCount>
{
    @XmlAttribute(name = "name", required = true)
    private String word;

    @XmlValue
    private int count;

    @XmlTransient
    private boolean sortByCount; // else by word

    @Override public int compareTo(WordCount otherWordCount)
    {
        if (sortByCount)
        {
            return ComparisonChain.start().compare(count, otherWordCount.count).result();
        }
        else
        {
            return ComparisonChain.start().compare(word, otherWordCount.word).result();
        }
    }
}
