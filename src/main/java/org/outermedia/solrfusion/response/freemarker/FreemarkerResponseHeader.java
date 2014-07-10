package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Data holder class to represent a response header in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerResponseHeader
{

    @Getter
    private String query;

    @Getter
    private String filterQuery;

    @Getter
    private int rows;

    public FreemarkerResponseHeader(ClosableIterator<Document, SearchServerResponseInfo> docStream, String query,
        String filterQuery)
    {
        this.rows = docStream.size();
        this.query = query;
        this.filterQuery = filterQuery;
    }
}
