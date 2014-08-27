package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.*;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FacetHit;
import org.outermedia.solrfusion.response.parser.ResponseSection;
import org.outermedia.solrfusion.response.parser.WordCount;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 7/11/14.
 */
public class PagingResponseConsolidatorTest
{
    TestHelper helper;
    Configuration cfg;

    static class TestResponseSection extends ResponseSection
    {
        @Override protected void addFacetValueAsDocField(Document facetDocument, FacetHit hit)
        {
            super.addFacetValueAsDocField(facetDocument, hit);
        }
    }

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
    }

    @Test
    public void testSortingEmpty() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList(), result);
    }

    @Test
    public void testSortingNoHits() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        createResponses(consolidator, new String[]{}, new String[]{}, null, null);
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList(), result);
        result = sort(consolidator, 4, 4, false);
        Assert.assertEquals("Expected different second page", Arrays.asList(), result);
    }

    @Test
    public void testSortingExactPage() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        createResponses(consolidator, new String[]{"c", "a"}, new String[]{"b", "d"}, null, null);
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList("d", "c", "b", "a"), result);
    }

    @Test
    public void testSortingMany() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        createResponses(consolidator, new String[]{"c", "a", "w", "b"}, new String[]{"b", "d", "z", "f", "aa"}, null,
            null);
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList("z", "w", "f", "d"), result);
        result = sort(consolidator, 4, 4, false);
        Assert.assertEquals("Expected different second page", Arrays.asList("c", "b", "b", "aa"), result);
        result = sort(consolidator, 8, 4, false);
        Assert.assertEquals("Expected different third page", Arrays.asList("a"), result);
        result = sort(consolidator, 12, 4, false);
        Assert.assertEquals("Expected different fourth page", Arrays.asList(), result);
    }

    @Test
    public void testSortingManyAsc() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);
        createResponses(consolidator, new String[]{"c", "a", "w", "b"}, new String[]{"b", "d", "z", "f", "aa"}, null,
            null);
        List<String> result = sort(consolidator, 0, 4, true);
        Assert.assertEquals("Expected different first page", Arrays.asList("a", "aa", "b", "b"), result);
        result = sort(consolidator, 4, 4, true);
        Assert.assertEquals("Expected different second page", Arrays.asList("c", "d", "f", "w"), result);
        result = sort(consolidator, 8, 4, true);
        Assert.assertEquals("Expected different third page", Arrays.asList("z"), result);
    }

    protected void createResponses(ResponseConsolidatorIfc consolidator, String[] titles1, String[] titles2,
        List<FacetHit> facetFields1, List<FacetHit> facetFields2)
    {
        addAnswerFromServer("Bibliothek9000", consolidator, titles1, buildFacetDoc(facetFields1));
        addAnswerFromServer("Bibliothek9002", consolidator, titles2, buildFacetDoc(facetFields2));
        Assert.assertEquals("Number of added responses is different", 2, consolidator.numberOfResponseStreams());
    }

    protected List<String> sort(ResponseConsolidatorIfc consolidator, int start, int pageSize, boolean sortAsc)
        throws InvocationTargetException, IllegalAccessException
    {
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setSortSpec(new SortSpec("title", null, sortAsc));
        fusionRequest.setStart(new SolrFusionRequestParam(String.valueOf(start)));
        fusionRequest.setPageSize(new SolrFusionRequestParam(String.valueOf(pageSize)));
        ClosableIterator<Document, SearchServerResponseInfo> docIt = consolidator.getResponseIterator(fusionRequest);
        return collectTitles(docIt);
    }

    private List<String> collectTitles(ClosableIterator<Document, SearchServerResponseInfo> docIt)
    {
        List<String> result = new ArrayList<>();
        while (docIt.hasNext())
        {
            Document doc = docIt.next();
            result.add(doc.getFieldTermByFusionName("title").getFusionFieldValue().get(0));
        }
        return result;
    }

    protected void addAnswerFromServer(String serverName, ResponseConsolidatorIfc consolidator, String[] titles,
        Document facetFields)
    {
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName(serverName);
        FusionRequest fusionRequest = new FusionRequest();
        ClosableIterator<Document, SearchServerResponseInfo> documents = createDocuments(serverName, fusionRequest,
            titles);
        consolidator.addResultStream(serverConfig, documents, fusionRequest, null, facetFields);
    }

    protected ClosableIterator<Document, SearchServerResponseInfo> createDocuments(String serverName,
        FusionRequest fusionRequest, String... titles)
    {
        String titleField = "title";
        if (serverName.equals("Bibliothek9002"))
        {
            titleField = "titleVT_de";
        }
        fusionRequest.setSearchServerSortField(titleField);
        List<Document> docs = new ArrayList<>();
        int id = 1;
        for (String t : titles)
        {
            Document doc = new Document();
            doc.addField("id", String.valueOf(id++));
            doc.addField("score", "0.35");
            doc.addField(titleField, t);
            docs.add(doc);
        }
        SearchServerResponseInfo info = new SearchServerResponseInfo(titles.length, null, null, null);
        return new ClosableListIterator<>(docs, info);
    }

    @Test
    public void testFacetHandling() throws InvocationTargetException, IllegalAccessException, UnmarshalException
    {
        PagingResponseConsolidator consolidator = (PagingResponseConsolidator) PagingResponseConsolidator.Factory.getInstance();
        consolidator.initConsolidator(cfg);

        createResponses(consolidator, new String[]{"a"}, new String[]{"b"},
            Arrays.asList(buildFaceHit("title", "a", "b"), buildFaceHit("language", "a", "b")),
            Arrays.asList(buildFaceHit("titleVT_de", "A", "B"), buildFaceHit("titleVT_eng", "A", "C"),
                buildFaceHit("language", "A", "B")));

        IdGeneratorIfc idGen = cfg.getIdGenerator();
        String fusionIdField = idGen.getFusionIdField();
        FusionRequest req = new FusionRequest();
        Map<String, List<WordCount>> fusionFacetFields = consolidator.mapFacetWordCounts(idGen, fusionIdField, req);
        // System.out.println("FACETS " + fusionFacetFields);
        Assert.assertEquals("Different facet number than expected", 3, fusionFacetFields.size());
        String theKey = "title";
        Assert.assertEquals("Expected other facet field", "title", theKey);
        List<WordCount> facet = fusionFacetFields.get(theKey);
        // word counts of "A" are added
        List<WordCount> expectedMap = buildWordCountMap("A", 2, "B", 2, "C", 2, "a", 1, "b", 2);
        Assert.assertEquals("Expected other facets", expectedMap, facet);
        Assert.assertTrue("Didn't find language in " + fusionFacetFields.keySet(),
            fusionFacetFields.containsKey("language"));
        // the last mapping sets the fusion field's name
        Assert.assertTrue("Didn't find language_en in " + fusionFacetFields.keySet(),
            fusionFacetFields.containsKey("language_en"));
        expectedMap = buildWordCountMap("A", 1, "B", 2);
        Assert.assertEquals("Expected other facets", expectedMap, fusionFacetFields.get("language_en"));
        expectedMap = buildWordCountMap("a", 1, "b", 2);
        Assert.assertEquals("Expected other facets", expectedMap, fusionFacetFields.get("language"));
    }

    protected Document buildFacetDoc(List<FacetHit> facets)
    {
        if (facets == null)
        {
            return null;
        }

        TestResponseSection rs = new TestResponseSection();
        Document facetDoc = new Document();
        for (FacetHit fh : facets)
        {
            rs.addFacetValueAsDocField(facetDoc, fh);
        }
        facetDoc.setSearchServerDocId("id", "1");
        return facetDoc;
    }

    protected FacetHit buildFaceHit(String searchServerField, String w1, String w2) throws UnmarshalException
    {
        FacetHit fh = new FacetHit();
        fh.setSearchServerFieldName(searchServerField);
        List<WordCount> fieldCounts = new ArrayList<>();
        fieldCounts.add(buildWordCount(w1, 1));
        fieldCounts.add(buildWordCount(w2, 2));
        fh.setFieldCounts(fieldCounts);
        return fh;
    }

    protected WordCount buildWordCount(String w, int count)
    {
        WordCount wc = new WordCount();
        wc.setWord(w);
        wc.setCount(count);
        return wc;
    }

    protected List<WordCount> buildWordCountMap(Object... entries)
    {
        List<WordCount> expectedMap = new ArrayList<>();
        for (int i = 0; i < entries.length; i += 2)
        {
            WordCount wc = new WordCount();
            wc.setWord((String) entries[i]);
            wc.setCount((Integer) entries[i + 1]);
            expectedMap.add(wc);
        }
        return expectedMap;
    }
}
