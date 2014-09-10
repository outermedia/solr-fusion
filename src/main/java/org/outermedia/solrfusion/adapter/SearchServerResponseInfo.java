package org.outermedia.solrfusion.adapter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.WordCount;

import java.util.List;
import java.util.Map;

/**
 * Context added to a ClosableIterator in order to store highlights, facets, 'match' doc (morelikethis) and the total
 * number of found documents. The iterator stores the found Solr documents.
 *
 * Created by ballmann on 6/12/14.
 */
@ToString
@Getter
@Setter
public class SearchServerResponseInfo
{
    private Map<String, Document> highlighting;
    private Map<String, List<WordCount>> facetFields;
    private List<Document> allMatchDocs;
    private int totalNumberOfHits;

    public SearchServerResponseInfo(int totalNumber, Map<String, Document> allHighlighting,
        Map<String, List<WordCount>> facetFields, List<Document> allMatchDocs)
    {
        totalNumberOfHits = totalNumber;
        highlighting = allHighlighting;
        this.facetFields = facetFields;
        this.allMatchDocs = allMatchDocs;
    }
}
