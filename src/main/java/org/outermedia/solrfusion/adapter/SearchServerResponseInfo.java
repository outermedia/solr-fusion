package org.outermedia.solrfusion.adapter;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
