package org.outermedia.solrfusion.response;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 7/11/14.
 */
public class MappingClosableIteratorTest
{
    TestHelper helper;
    Configuration cfg;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
    }

    @Test
    public void testIgnoreUnmappedDocs() throws InvocationTargetException, IllegalAccessException
    {
        ClosableIterator<Document, SearchServerResponseInfo> docs = createDocuments("a", "b", "c");
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName("Bibliothek 9000");
        MappingClosableIterator mapper = new MappingClosableIterator(docs, cfg, serverConfig, null, ResponseTarget.ALL, true);
        // neither id nor score is set, and "auto" isn't mapped, so no document should be returned
        Assert.assertFalse("Expected no result", mapper.hasNext());
    }

    protected ClosableIterator<Document, SearchServerResponseInfo> createDocuments(String... titles)
    {
        List<Document> docs = new ArrayList<>();
        for (String t : titles)
        {
            Document doc = new Document();
            doc.addField("auto", t);
            docs.add(doc);
        }
        SearchServerResponseInfo info = new SearchServerResponseInfo(titles.length, null, null, null);
        return new ClosableListIterator<>(docs, info);
    }
}
