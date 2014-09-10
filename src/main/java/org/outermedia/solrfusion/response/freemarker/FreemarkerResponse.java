package org.outermedia.solrfusion.response.freemarker;

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