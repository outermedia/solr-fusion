package org.outermedia.solrfusion.response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
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
        MappingClosableIterator mapper = new MappingClosableIterator(docs, cfg, serverConfig, null);
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
        SearchServerResponseInfo info = new SearchServerResponseInfo(titles.length);
        return new ClosableListIterator<>(docs, info);
    }
}
