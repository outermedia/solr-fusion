package org.outermedia.solrfusion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.response.HighlightingMap;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 7/29/14.
 */
public class MergeStrategyTest
{
    TestHelper helper;
    Configuration cfg;
    String sep = DefaultIdGenerator.SEPARATOR;
    String isep = DefaultIdGenerator.ID_SEPARATOR;


    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-doc-merging-fusion-schema.xml");
    }

    @Test
    public void testMergeOneDoc() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("BibliothekC", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(cfg.getIdGenerator());
        List<Document> mergedDoc = merger.mergeDocuments("isbn", cfg, docs, emptyHighlights, null);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "BibliothekC" + sep + "3", "title=t3", "isbn[1]=i1", "author=a2");
    }

    @Test
    public void testMergeAllSeveralDocs() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("BibliothekA", "1"), "title", "t1", "isbn", "i1"));
        docs.add(buildDocument("id", fusionId("BibliothekB", "2"), "title", "t2", "isbn", "i1", "author", "a1"));
        docs.add(buildDocument("id", fusionId("BibliothekC", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(cfg.getIdGenerator());
        List<Document> mergedDoc = merger.mergeDocuments("isbn", cfg, docs, emptyHighlights, null);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc,
            "id=BibliothekA" + sep + "1" + isep + "BibliothekB" + sep + "2" + isep + "BibliothekC" + sep + "3",
            "title=t1", "isbn[1]=i1", "author=a1");
    }

    @Test
    public void testMergeSomeSeveralDocs() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("BibliothekA", "1"), "title", "t1", "isbn", "i1"));
        docs.add(buildDocument("id", fusionId("BibliothekC", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(cfg.getIdGenerator());
        List<Document> mergedDoc = merger.mergeDocuments("isbn", cfg, docs, emptyHighlights, null);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "id=BibliothekA" + sep + "1" + isep + "BibliothekC" + sep + "3", "title=t1", "isbn[1]=i1",
            "author=a2");
    }

    @Test
    public void testMergeSomeSeveralDocsFirstMissing() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("BibliothekB", "2"), "isbn", "i1", "author", "a1"));
        docs.add(buildDocument("id", fusionId("BibliothekC", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(cfg.getIdGenerator());
        List<Document> mergedDoc = merger.mergeDocuments("isbn", cfg, docs, emptyHighlights, null);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        String sep = DefaultIdGenerator.SEPARATOR;
        String isep = DefaultIdGenerator.ID_SEPARATOR;
        checkDoc(mergedDoc, "BibliothekB" + sep + "2" + isep + "BibliothekC" + sep + "3", "title=t3", "isbn[1]=i1",
            "author=a1");
    }

    private void checkDoc(List<Document> mergedDocs, String... fields)
    {
        Assert.assertEquals("Expected only one merged doc", 1, mergedDocs.size());
        String docStr = mergedDocs.get(0).buildFusionDocStr();
        for (String f : fields)
        {
            Assert.assertTrue("Didn't find " + f + " in " + docStr, docStr.contains(f + "\n"));
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
        for (int i = 0; i < fields.length; i += 2)
        {
            String name = (String) fields[i];
            FusionField fusionField = cfg.findFieldByName(name);
            Object value = fields[i + 1];
            if (value instanceof List)
            {
                doc.addFusionField(name, fusionField, (List<String>) value, null);
            }
            else
            {
                List<String> valueList = Arrays.asList((String) value);
                doc.addFusionField(name, fusionField, valueList, null);
            }
            doc.getFieldTermByFusionName(name).setWasMapped(true);
        }
        return doc;
    }

    @Test
    public void testMergeSomeSeveralDocsSameServer() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("BibliothekA", "1"), "title", "t1a", "isbn", Arrays.asList("ia", "ic"),
            "author", "a1"));
        docs.add(buildDocument("id", fusionId("BibliothekA", "2"), "title", "t1b", "isbn", Arrays.asList("ib", "ic"),
            "author", "a2"));
        docs.add(buildDocument("id", fusionId("BibliothekC", "3"), "title", "t3a", "isbn", Arrays.asList("ic", "id"),
            "other1", "a3"));
        docs.add(buildDocument("id", fusionId("BibliothekC", "4"), "title", "t3b", "isbn", Arrays.asList("ic", "ie"),
            "other2", "a4"));
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(cfg.getIdGenerator());
        List<Document> mergedDocs = merger.mergeDocuments("isbn", cfg, docs, emptyHighlights, null);
        Assert.assertNotNull("Expected merged document", mergedDocs);
        for (Document d : mergedDocs)
        {
            System.out.println("MD " + d.buildFusionDocStr());
        }
    }
}
