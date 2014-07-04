package org.outermedia.solrfusion;

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ControllerTest
{
    protected TestHelper helper;

    @Mock
    ResponseRendererIfc testRenderer;

    ByteArrayInputStream testResponse;

    @Mock
    SearchServerAdapterIfc testAdapter;

    @Mock
    SearchServerAdapterIfc testAdapter9000;

    @Mock
    SearchServerAdapterIfc testAdapter9002;

    Configuration cfg;

    @Mock
    private SearchServerConfig testSearchConfig;

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
        initTestResponse();
    }

    protected void initTestResponse() throws IOException
    {
        byte[] emptyResponseBytes = Files.toByteArray(new File("target/test-classes/test-empty-xml-response.xml"));
        testResponse = new ByteArrayInputStream(emptyResponseBytes);
    }

    @Test
    public void testProcess()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    protected FusionControllerIfc createTestFusionController(String fusionSchema)
        throws IOException, JAXBException, SAXException, ParserConfigurationException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        cfg = spy(helper
            .readFusionSchemaWithoutValidation(fusionSchema));
        when(testRenderer.getResponseString(any(ClosableIterator.class), anyString(), anyString())).thenReturn(
            "<xml>42</xml>");
        when(cfg.getResponseRendererByType(any(ResponseRendererType.class))).thenReturn(testRenderer);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        if (searchServerConfigs != null && !searchServerConfigs.isEmpty())
        {
            SearchServerConfig searchServerConfig = spy(searchServerConfigs.get(0));
            searchServerConfigs.clear();
            searchServerConfigs.add(searchServerConfig);
            when(searchServerConfig.getInstance()).thenReturn(testAdapter);
            when(testAdapter.sendQuery(Mockito.anyMapOf(String.class, String.class), Mockito.anyInt())).thenReturn(
                testResponse);
        }
        return cfg.getController();
    }

    @Test
    public void testWrongRenderer()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        Configuration cfg = spy(helper
            .readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
        when(testRenderer.getResponseString(any(ClosableIterator.class), anyString(), anyString())).thenReturn(
            "<xml>42</xml>");
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig configuredSearchServer = spy(searchServerConfigs.get(0));
        searchServerConfigs.clear();
        searchServerConfigs.add(configuredSearchServer);
        when(configuredSearchServer.getInstance()).thenReturn(testAdapter);
        when(testAdapter.sendQuery(anyMapOf(String.class, String.class), Mockito.anyInt())).thenReturn(testResponse);
        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        // response format not set
        fusionRequest.setResponseType(null);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for not specified response type", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected",
            "Found no configuration for response renderer: <unknown>", fusionResponse.getErrorMessage());

        // first fc.process() consumed test response, so re-init it and bind the new object to the testAdapter again
        initTestResponse();
        when(testAdapter.sendQuery(anyMapOf(String.class, String.class), Mockito.anyInt())).thenReturn(testResponse);
        // renderer specified, but not configured
        cfg.getSearchServerConfigs().getResponseRendererFactories().clear();
        fusionRequest.setResponseType(ResponseRendererType.JSON);
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for unknown response type", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected",
            "Found no configuration for response renderer: JSON", fusionResponse.getErrorMessage());
    }

    @Test
    public void testTooLessResponses()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for too less server responses", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", cfg.getDisasterMessage().getText(),
            fusionResponse.getErrorMessage());
    }

    @Test
    public void testSearchServersConfigured()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for no servers configured", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "No search server configured at all.",
            fusionResponse.getErrorMessage());
    }

    @Test
    public void testQueryParsingFailed()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:*:Schiller");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for bad query", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "Query parsing failed: author:*:Schiller",
            fusionResponse.getErrorMessage());
    }

    @Test
    public void testQueryWithMultipleServersAndResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        testMultipleServers("target/test-classes/test-xml-response-9000.xml",
            "target/test-classes/test-xml-response-9002.xml");
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc"), 4000);
        verify(testAdapter9002, times(1)).sendQuery(buildParams("titleVT_eng:abc"), 4000);
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml");
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "    <str name=\"rows\">0</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc"), 4000);
        verify(testAdapter9002, times(1)).sendQuery(buildParams("titleVT_eng:abc"), 4000);
    }

    protected Map<String, String> buildParams(String q)
    {
        Map<String, String> result = new HashMap<>();
        result.put(SolrFusionRequestParams.QUERY.getRequestParamName(), q);
        return result;
    }

    protected String testMultipleServers(String responseServer1, String responseServer2)
        throws IOException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, URISyntaxException
    {
        byte[] documents9000 = Files.toByteArray(new File(responseServer1));
        byte[] documents9002 = Files.toByteArray(new File(responseServer2));
        ByteArrayInputStream documents9000Stream = new ByteArrayInputStream(documents9000);
        ByteArrayInputStream documents9002Stream = new ByteArrayInputStream(documents9002);

        cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
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
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(anyMapOf(String.class, String.class),
            Mockito.anyInt());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(anyMapOf(String.class, String.class),
            Mockito.anyInt());

        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("title:abc");
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
