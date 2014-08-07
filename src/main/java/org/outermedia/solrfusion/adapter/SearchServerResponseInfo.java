package org.outermedia.solrfusion.adapter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.Map;

/**
 * Created by ballmann on 6/12/14.
 */
@ToString
@Getter
@Setter
public class SearchServerResponseInfo
{
    private final Map<String, Document> highlighting;
    private int totalNumberOfHits;

    public SearchServerResponseInfo(int totalNumber, Map<String, Document> allHighlighting)
    {
        totalNumberOfHits = totalNumber;
        highlighting = allHighlighting;
    }
}
