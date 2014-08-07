package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Data holder class to represent a response header in the freemarker template.
 *
 * @author stephan
 */
@Getter
public class FreemarkerResponseHeader
{

    private String query;

    private String filterQuery;

    private int rows;

    private String sort;

    private String fields;

    private String highlight;

    private String highlightPre;

    private String highlightPost;

    private String highlightFields;

    private String highlightQuery;

    public FreemarkerResponseHeader(ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request)
    {
        this.rows = 0;
        if(docStream != null)
        {
            rows = docStream.size();
        }
        this.query = request.getQuery();
        if(this.query == null) this.query = "";
        this.filterQuery = request.getFilterQuery();
        this.sort = request.getSolrFusionSortField();
        this.fields = request.getFieldsToReturn();
        this.highlight = request.getHighlight();
        this.highlightPre = request.getHighlightPre();
        this.highlightPost = request.getHighlightPost();
        this.highlightFields = request.getHighlightingFieldsToReturn();
        this.highlightQuery = request.getHighlightQuery();
    }
}
