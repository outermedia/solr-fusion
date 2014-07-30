package org.outermedia.solrfusion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ballmann on 7/29/14.
 */
public class MergeStrategyTest
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
    public void testMergeOneDoc() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("Bibliothek C", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        Document mergedDoc = merger.mergeDocuments(cfg, docs);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "Bibliothek_C-3", "title=t3", "isbn=i1", "author=a2");
    }

    @Test
    public void testMergeAllSeveralDocs() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("Bibliothek A", "1"), "title", "t1", "isbn", "i1"));
        docs.add(buildDocument("id", fusionId("Bibliothek B", "2"), "title", "t2", "isbn", "i1", "author", "a1"));
        docs.add(buildDocument("id", fusionId("Bibliothek C", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        Document mergedDoc = merger.mergeDocuments(cfg, docs);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "id=Bibliothek_A-1\\u002cBibliothek_B-2\\u002cBibliothek_C-3", "title=t1", "isbn=i1",
            "author=a1");
    }

    @Test
    public void testMergeSomeSeveralDocs() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("Bibliothek A", "1"), "title", "t1", "isbn", "i1"));
        docs.add(buildDocument("id", fusionId("Bibliothek C", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        Document mergedDoc = merger.mergeDocuments(cfg, docs);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "id=Bibliothek_A-1\\u002cBibliothek_C-3", "title=t1", "isbn=i1", "author=a2");
    }

    @Test
    public void testMergeSomeSeveralDocsFirstMissing() throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = cfg.getMerger();
        Set<Document> docs = new HashSet<>();
        docs.add(buildDocument("id", fusionId("Bibliothek B", "2"), "isbn", "i1", "author", "a1"));
        docs.add(buildDocument("id", fusionId("Bibliothek C", "3"), "title", "t3", "isbn", "i1", "author", "a2"));
        Document mergedDoc = merger.mergeDocuments(cfg, docs);
        Assert.assertNotNull("Expected merged document", mergedDoc);
        // System.out.println("MD " + mergedDoc.buildFusionDocStr());
        checkDoc(mergedDoc, "Bibliothek_B-2\\u002cBibliothek_C-3", "title=t3", "isbn=i1", "author=a1");
    }

    private void checkDoc(Document mergedDoc, String... fields)
    {
        String docStr = mergedDoc.buildFusionDocStr();
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
                doc.addFusionField(name, fusionField, (List<String>) value);
            }
            else
            {
                List<String> valueLIst = Arrays.asList((String) value);
                doc.addFusionField(name, fusionField, valueLIst);
            }
            doc.getFieldTermByFusionName(name).setWasMapped(true);
        }
        return doc;
    }
}
