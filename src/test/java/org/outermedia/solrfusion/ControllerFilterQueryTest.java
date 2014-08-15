package org.outermedia.solrfusion;

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ControllerFilterQueryTest extends AbstractControllerTest
{
    @Test
    public void testProcessWithoutFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        checkFilterQueryHandling(null);
    }

    @Test
    public void testProcessWithOneFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        // with one fq
        checkFilterQueryHandling(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow")));
    }

    @Test
    public void testProcessWithTwoFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        checkFilterQueryHandling(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow"),
            new SolrFusionRequestParam("author:Wolfgang")));
    }

    protected void checkFilterQueryHandling(List<SolrFusionRequestParam> filterQueries)
        throws IllegalAccessException, ParserConfigurationException, IOException, JAXBException, URISyntaxException,
        SAXException, InvocationTargetException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen"));
        fusionRequest.setFilterQuery(filterQueries);
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField("score");
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error: " + fusionResponse.getErrorMessage(), fusionResponse.isOk());
    }

    @Test
    public void testQueryParsingFailed()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen"));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("author:*:Schiller")));

        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for bad query", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected",
            "Query parsing failed: author:*:Schiller;\nCause: ERROR: Parsing of query author:*:Schiller failed.\n" +
                "Cannot interpret query 'author:*:Schiller': '*' or '?' not allowed as first character in WildcardQuery\n" +
                "'*' or '?' not allowed as first character in WildcardQuery", fusionResponse.getErrorMessage().trim());
    }

    @Test
    public void testQueryWithMultipleServersAndResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        testMultipleServers("title:abc", "title:def", "target/test-classes/test-xml-response-9000.xml",
            "target/test-classes/test-xml-response-9002.xml");
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title:def"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(buildParams("titleVT_eng:abc", "titleVT_eng:def"), 4000,
            "3.6");
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:def", "target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"sort\"><![CDATA[score]]></str>\n" +
            "    <arr name=\"fq\">\n" +
            "        <str>title:def</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title:def"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(buildParams("titleVT_eng:abc", "titleVT_eng:def"), 4000,
            "3.6");
    }

    protected String testMultipleServers(String queryStr, String filterQueryStr, String responseServer1,
        String responseServer2)
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        byte[] documents9000 = Files.toByteArray(new File(responseServer1));
        byte[] documents9002 = Files.toByteArray(new File(responseServer2));
        ByteArrayInputStream documents9000Stream = new ByteArrayInputStream(documents9000);
        ByteArrayInputStream documents9002Stream = new ByteArrayInputStream(documents9002);

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
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
        testAdapter9000 = spy(searchServerConfig9000.getInstance());
        when(searchServerConfig9000.getInstance()).thenReturn(testAdapter9000);
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(any(Multimap.class), Mockito.anyInt(),
            anyString());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(any(Multimap.class), Mockito.anyInt(),
            anyString());

        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam(filterQueryStr)));
        fusionRequest.setPageSize(10);
        fusionRequest.setStart(0);
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(spyCfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());

        String result = fusionResponse.getResponseAsString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        // System.out.println("RESPONSE " + result);
        return result;
    }
}
