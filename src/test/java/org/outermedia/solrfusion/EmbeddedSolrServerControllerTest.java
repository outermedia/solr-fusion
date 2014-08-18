package org.outermedia.solrfusion;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.solr.EmbeddedSolrAdapter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class EmbeddedSolrServerControllerTest extends SolrServerDualTestBase
{
    protected TestHelper helper;

    @Mock ResponseRendererIfc testRenderer;

    ByteArrayInputStream testResponse;

    @Mock SearchServerAdapterIfc testAdapter;

    EmbeddedSolrAdapter testAdapter9000;

    EmbeddedSolrAdapter testAdapter9002;

    Configuration cfg;

    @Mock
    private SearchServerConfig testSearchConfig;

    @Before
    public void fillSolr() throws IOException, SolrServerException
    {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", String.valueOf(1));
        document.addField("title", String.valueOf("abc"));
        document.addField("author", String.valueOf("Shakespeare"));
        firstServer.add(document);
        firstTestServer.commitLastDocs();

        document = new SolrInputDocument();
        document.addField("id", String.valueOf(1));
        document.addField("titleVT_eng", String.valueOf("abc"));
        document.addField("author", String.valueOf("Shakespeare"));
        secondTestServer.getServer().add(document);
        secondTestServer.commitLastDocs();
    }

    @After
    public void cleanSolr() throws IOException, SolrServerException
    {
        firstServer.deleteByQuery("*:*");
    }

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:xyz", "title:XYZ");
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:xyz]]></str>\n" +
            "    <str name=\"sort\"><![CDATA[score]]></str>\n" +
            "    <str name=\"fl\"><![CDATA[id title score]]></str>\n" +
            "    <arr name=\"fq\">\n" +
            "        <str>title:XYZ</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expectedXml, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(
            buildMap(QUERY, "title:xyz", FILTER_QUERY, "title:XYZ", PAGE_SIZE, "10", START, "0", SORT, "score desc",
                FIELDS_TO_RETURN, "id title score", WRITER_TYPE, "xml"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(
            buildMap(QUERY, "titleVT_eng:xyz", FILTER_QUERY, "titleVT_eng:XYZ", PAGE_SIZE, "10", START, "0", SORT,
                "score desc", FIELDS_TO_RETURN, "id titleVT_de titleVT_eng score", WRITER_TYPE, "xml"), 4000, "3.6");
    }

    protected Multimap<String> buildMap(Object... v)
    {
        Multimap<String> result = new Multimap<>();
        for (int i = 0; i < v.length; i += 2)
        {
            result.put(((SolrFusionRequestParams) v[i]), (String) v[i + 1]);
        }
        return result;
    }

    @Test
    public void testQueryWithMultipleServersAndResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:abc");
        String sep = DefaultIdGenerator.SEPARATOR;
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"rows\"><![CDATA[2]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"sort\"><![CDATA[score]]></str>\n" +
            "    <str name=\"fl\"><![CDATA[id title score]]></str>\n" +
            "    <arr name=\"fq\">\n" +
            "        <str>title:abc</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"2\" start=\"0\">\n" +
            "    <doc>\n" +
            "        <str name=\"id\">Bibliothek9002" + sep + "1</str>\n" +
            "        <str name=\"title\">abc</str>\n" +
            "        <float name=\"score\">0.6750762040000001</float>\n" +
            "    </doc>\n" +
            "    <doc>\n" +
            "        <str name=\"id\">Bibliothek9000" + sep + "1</str>\n" +
            "        <float name=\"score\">0.36822338400000004</float>\n" +
            "        <str name=\"title\">abc</str>\n" +
            "    </doc>\n" +
            "</result>\n" +
            "</response>";

        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(
            buildMap(QUERY, "title:abc", FILTER_QUERY, "title:abc", PAGE_SIZE, "10", START, "0", SORT, "score desc",
                FIELDS_TO_RETURN, "id title score", WRITER_TYPE, "xml"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(
            buildMap(QUERY, "titleVT_eng:abc", FILTER_QUERY, "titleVT_eng:abc", PAGE_SIZE, "10", START, "0", SORT,
                "score desc", FIELDS_TO_RETURN, "id titleVT_de titleVT_eng score", WRITER_TYPE, "xml"), 4000, "3.6");
    }

    protected String testMultipleServers(String queryStr, String filterQueryStr)
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-embedder-solr-adapter.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        List<SearchServerConfig> searchServerConfigs = spyCfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig searchServerConfig9000 = spy(searchServerConfigs.get(0));
        SearchServerConfig searchServerConfig9002 = spy(searchServerConfigs.get(1));
        searchServerConfigs.clear();

        searchServerConfigs.add(searchServerConfig9000);
        testAdapter9000 = (EmbeddedSolrAdapter) spy(searchServerConfig9000.getInstance());
        when(searchServerConfig9000.getInstance()).thenReturn(testAdapter9000);
        testAdapter9000.setTestServer(firstTestServer);

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = (EmbeddedSolrAdapter) spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        testAdapter9002.setTestServer(secondTestServer);

        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam(filterQueryStr)));
        fusionRequest.setResponseType(ResponseRendererType.XML);
        fusionRequest.setStart(0);
        fusionRequest.setPageSize(10);
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        fusionRequest.setFieldsToReturn(
            new SolrFusionRequestParam("id title " + fusionRequest.getSolrFusionSortField()));
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(spyCfg, fusionRequest, fusionResponse);
        System.out.println("RESPONSE " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());

        String result = fusionResponse.getResponseAsString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        System.out.println("RESPONSE " + result);
        return result;
    }

}
