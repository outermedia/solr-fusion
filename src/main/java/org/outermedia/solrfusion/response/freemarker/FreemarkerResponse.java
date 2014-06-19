package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder class to represent a response in the freemarker template.
 *
 * @author stephan
 */
public class FreemarkerResponse
{
    @Getter
    private int totalHitNumber;

    @Getter
    private List<FreemarkerDocument> documents;

    public FreemarkerResponse(ClosableIterator<Document, SearchServerResponseInfo> docStream)
    {
        this.totalHitNumber = docStream.getExtraInfo().getTotalNumberOfHits();
        this.documents = new ArrayList<>();

        Document d;
        FreemarkerDocument freemarkerDocument;
        while (docStream.hasNext())
        {
            freemarkerDocument = new FreemarkerDocument();
            d = docStream.next();
            d.accept(freemarkerDocument, null);
            documents.add(freemarkerDocument);
        }

    }

}