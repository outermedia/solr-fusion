package org.outermedia.solrfusion.response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.DefaultIdGenerator;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 7/11/14.
 */
public class PagingResponseConsolidatorMergeTest
{
    TestHelper helper;
    Configuration cfg;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-doc-merging-fusion-schema.xml");
    }

    @Test
    public void testDocMergingOnSingleValue() throws InvocationTargetException, IllegalAccessException
    {
        Document d1 = buildDocument("Id", "1", "Title", "t1", "ISBN", "i1", "Other1", "o1");
        Highlighting hl1 = createHighlighting("1");
        Document d2 = buildDocument("Id", "2", "Title", "t2", "ISBN", "i1", "Author", "a1", "Other2", "o2");
        Highlighting hl2 = createHighlighting("2");
        // d3 contains deleted field, which should stay deleted after merging
        Document d3 = buildDocument("Id", "3", "Title", "t3", "ISBN", "i1", "Author", "a2", "Unknown", "u1", "Other3",
            "o3");
        Highlighting hl3 = createHighlighting("3");
        // d4 contains no merge field; d4 must not be merged
        Document d4 = (buildDocument("Id", "4", "Title", "t4", "Other1", "o1x", "Other3", "o3x"));
        Highlighting hl4 = createHighlighting("4");

        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        addAnswerFromServer("BibliothekA", consolidator, Arrays.asList(d1, d4), Arrays.asList(hl1, hl4));
        addAnswerFromServer("BibliothekB", consolidator, Arrays.asList(d2), Arrays.asList(hl2));
        addAnswerFromServer("BibliothekC", consolidator, Arrays.asList(d3), Arrays.asList(hl3));

        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setSolrFusionSortField("id");
        fusionRequest.setSortAsc(true);
        fusionRequest.setStart(0);
        fusionRequest.setPageSize(100);
        ClosableIterator<Document, SearchServerResponseInfo> docs = consolidator.getResponseIterator(fusionRequest);
        String sep = DefaultIdGenerator.SEPARATOR;
        String isep = DefaultIdGenerator.ID_SEPARATOR;
        String expectedDoc1 = "score=0.42719999999999997\n" +
            "id=BibliothekA" + sep + "1" + isep + "BibliothekB" + sep + "2" + isep + "BibliothekC" + sep + "3\n" +
            "title=t1\n" +
            "isbn=i1\n" +
            "other1=o1\n" +
            "author=a1\n" +
            "other2=o2\n" +
            "other3=o3\n";
        String expectedDoc2 = "score=0.42719999999999997\n" +
            "id=BibliothekA" + sep + "4\n" +
            "title=t4\n" +
            "other1=o1x\n" +
            "other3=o3x\n";
        while (docs.hasNext())
        {
            Document doc = docs.next();
            String docStr = doc.buildFusionDocStr();
            if (expectedDoc1.equals(docStr))
            {
                continue;
            }
            if (expectedDoc2.equals(docStr))
            {
                continue;
            }
            Assert.fail("Got unexpected doc: " + docStr);
        }
        Map<String, Document> highlighting = docs.getExtraInfo().getHighlighting();
        Assert.assertNotNull("Expected highlights", highlighting);
        Assert.assertFalse("Expected some highlights", highlighting.isEmpty());
        expectedDoc1 = "id=BibliothekA_4\n" + "title=hlt4";
        expectedDoc2 = "id=BibliothekA_1-BibliothekB_2-BibliothekC_3\n" + "title=hlt1";
        for (String id : highlighting.keySet())
        {
            String docStr = highlighting.get(id).buildFusionDocStr();
            if (docStr.contains(expectedDoc1))
            {
                continue;
            }
            if (docStr.contains(expectedDoc2))
            {
                continue;
            }
            Assert.fail("Got unexpected doc: " + docStr);
        }
    }

    protected Highlighting createHighlighting(String id)
    {
        Highlighting hl = new Highlighting();
        hl.setDocId(id);
        hl.setDoc(buildDocument("Id", id, "Title", "hlt" + id));
        return hl;
    }

    @Test
    public void testDocMergingOnMultiValue() throws InvocationTargetException, IllegalAccessException
    {
        // patch fusion schema
        cfg.findFieldByName("isbn").asMultiValue();

        Document d1 = buildDocument("Id", "1", "Title", "t1", "ISBN", Arrays.asList("i1", "j1"), "Other1", "o1");
        Document d2 = buildDocument("Id", "2", "Title", "t2", "ISBN", Arrays.asList("j1", "i1"), "Author", "a1",
            "Other2", "o2");
        // d3 contains deleted field, which should stay deleted after merging
        Document d3 = buildDocument("Id", "3", "Title", "t3", "ISBN", Arrays.asList("j1"), "Author", "a2", "Unknown",
            "u1", "Other3", "o3");
        // d4 contains no merge field; d4 must not be merged
        Document d4 = (buildDocument("Id", "4", "Title", "t4", "Other1", "o1x", "Other3", "o3x"));

        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        addAnswerFromServer("BibliothekB", consolidator, Arrays.asList(d2), null);
        addAnswerFromServer("BibliothekA", consolidator, Arrays.asList(d1, d4), null);
        addAnswerFromServer("BibliothekC", consolidator, Arrays.asList(d3), null);

        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setSolrFusionSortField("id");
        fusionRequest.setSortAsc(true);
        fusionRequest.setStart(0);
        fusionRequest.setPageSize(100);
        ClosableIterator<Document, SearchServerResponseInfo> docs = consolidator.getResponseIterator(fusionRequest);
        String sep = DefaultIdGenerator.SEPARATOR;
        String isep = DefaultIdGenerator.ID_SEPARATOR;
        String expectedDoc1 = "score=0.42719999999999997\n" +
            "id=BibliothekA" + sep + "1" + isep + "BibliothekB" + sep + "2" + isep + "BibliothekC" + sep + "3\n" +
            "title=t1\n" +
            "other1=o1\n" +
            "author=a1\n" +
            "other2=o2\n" +
            "other3=o3\n" +
            "isbn[2]=i1,j1\n";
        String expectedDoc2 = "score=0.42719999999999997\n" +
            "id=BibliothekA" + sep + "4\n" +
            "title=t4\n" +
            "other1=o1x\n" +
            "other3=o3x\n";
        while (docs.hasNext())
        {
            Document doc = docs.next();
            String docStr = doc.buildFusionDocStr();
            if (expectedDoc1.equals(docStr))
            {
                continue;
            }
            if (expectedDoc2.equals(docStr))
            {
                continue;
            }
            Assert.fail("Got unexpected doc: " + docStr);
        }
    }

    protected String fusionId(String server, String docId) throws InvocationTargetException, IllegalAccessException
    {
        IdGeneratorIfc idGen = cfg.getIdGenerator();
        return idGen.computeId(server, docId);
    }

    protected Document buildDocument(Object... fields)
    {
        Document doc = new Document();
        doc.addField("score", "0.356");
        for (int i = 0; i < fields.length; i += 2)
        {
            String name = (String) fields[i];
            FusionField fusionField = cfg.findFieldByName(name);
            Object value = fields[i + 1];
            if (value instanceof List)
            {
                doc.addField(name, (List<String>) value);
            }
            else
            {
                doc.addField(name, (String) value);
            }
        }
        return doc;
    }

    protected void addAnswerFromServer(String serverName, ResponseConsolidatorIfc consolidator, List<Document> docs,
        List<Highlighting> highlights)
    {
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName(serverName);
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setSearchServerSortField("Id");
        ClosableIterator<Document, SearchServerResponseInfo> documents = createDocuments(docs);
        consolidator.addResultStream(serverConfig, documents, fusionRequest, highlights, null);
    }

    protected ClosableIterator<Document, SearchServerResponseInfo> createDocuments(List<Document> docs)
    {
        SearchServerResponseInfo info = new SearchServerResponseInfo(docs.size(), null, null, null);
        return new ClosableListIterator<>(docs, info);
    }
}
