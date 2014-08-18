package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

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
    private int totalMatchHitNumber;

    @Getter
    private String name;

    @Getter
    private List<FreemarkerDocument> documents;

    @Getter
    private List<FreemarkerDocument> matchDocuments;

    public FreemarkerResponse(Configuration configuration,
        ClosableIterator<Document, SearchServerResponseInfo> docStream)
    {
        this.totalHitNumber = 0;
        if (docStream != null)
        {
            totalHitNumber = docStream.getExtraInfo().getTotalNumberOfHits();
        }
        this.totalMatchHitNumber = 0;
        List<Document> allMatchDocs = null;
        if (docStream != null)
        {
            allMatchDocs = docStream.getExtraInfo().getAllMatchDocs();
            if (allMatchDocs != null && allMatchDocs.size() > 0)
            {
                totalMatchHitNumber = allMatchDocs.size();
            }
        }
        this.documents = new ArrayList<>();
        this.matchDocuments = new ArrayList<>();

        ScriptEnv env = new ScriptEnv();
        env.setConfiguration(configuration);

        prepareDocuments(docStream, env, documents);

        ClosableIterator<Document, SearchServerResponseInfo> matchDocStream = new ClosableListIterator<>(allMatchDocs,
            null);
        prepareDocuments(matchDocStream, env, matchDocuments);
    }

    protected void prepareDocuments(ClosableIterator<Document, SearchServerResponseInfo> docStream, ScriptEnv env,
        List<FreemarkerDocument> docs)
    {
        FreemarkerDocument freemarkerDocument;
        Document d;
        if (docStream != null)
        {
            while (docStream.hasNext())
            {
                freemarkerDocument = new FreemarkerDocument();
                d = docStream.next();
                d.accept(freemarkerDocument, env);
                docs.add(freemarkerDocument);
            }
        }
    }

}