package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
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
import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 7/11/14.
 */
public class PagingResponseConsolidatorTest
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
    public void testSortingEmpty() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.init(cfg);
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList(), result);
    }

    @Test
    public void testSortingNoHits() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.init(cfg);
        createResponses(consolidator, new String[]{}, new String[]{});
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList(), result);
        result = sort(consolidator, 4, 4, false);
        Assert.assertEquals("Expected different second page", Arrays.asList(), result);
    }

    @Test
    public void testSortingExactPage() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.init(cfg);
        createResponses(consolidator, new String[]{"c", "a"}, new String[]{"b", "d"});
        List<String> result = sort(consolidator, 0, 4, false);
        Assert.assertEquals("Expected different first page", Arrays.asList("d", "c", "b", "a"), result);
    }

    @Test
    public void testSortingMany() throws InvocationTargetException, IllegalAccessException
    {
        ResponseConsolidatorIfc consolidator = PagingResponseConsolidator.Factory.getInstance();
        consolidator.init(cfg);
        createResponses(consolidator, new String[]{"c", "a", "w", "b"}, new String[]{"b", "d", "z", "f", "aa"});
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
        consolidator.init(cfg);
        createResponses(consolidator, new String[]{"c", "a", "w", "b"}, new String[]{"b", "d", "z", "f", "aa"});
        List<String> result = sort(consolidator, 0, 4, true);
        Assert.assertEquals("Expected different first page", Arrays.asList("a", "aa", "b", "b"), result);
        result = sort(consolidator, 4, 4, true);
        Assert.assertEquals("Expected different second page", Arrays.asList("c", "d", "f", "w"), result);
        result = sort(consolidator, 8, 4, true);
        Assert.assertEquals("Expected different third page", Arrays.asList("z"), result);
    }

    protected void createResponses(ResponseConsolidatorIfc consolidator, String[] titles1, String[] titles2)
    {
        addAnswerFromServer("Bibliothek9000", consolidator, titles1);
        addAnswerFromServer("Bibliothek9002", consolidator, titles2);
        Assert.assertEquals("Number of added responses is different", 2, consolidator.numberOfResponseStreams());
    }

    protected List<String> sort(ResponseConsolidatorIfc consolidator, int start, int pageSize, boolean sortAsc)
        throws InvocationTargetException, IllegalAccessException
    {
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setSolrFusionSortField("title");
        fusionRequest.setStart(start);
        fusionRequest.setPageSize(pageSize);
        fusionRequest.setSortAsc(sortAsc);
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

    protected void addAnswerFromServer(String serverName, ResponseConsolidatorIfc consolidator, String[] titles)
    {
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName(serverName);
        FusionRequest fusionRequest = new FusionRequest();
        ClosableIterator<Document, SearchServerResponseInfo> documents = createDocuments(serverName, fusionRequest,
            titles);
        consolidator.addResultStream(serverConfig, documents, fusionRequest, null);
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
        SearchServerResponseInfo info = new SearchServerResponseInfo(titles.length, null);
        return new ClosableListIterator<>(docs, info);
    }
}
