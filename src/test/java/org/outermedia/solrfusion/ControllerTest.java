package org.outermedia.solrfusion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.mockito.Matchers.any;
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

    @Mock
    ClosableIterator<Document,SearchServerResponseInfo> testResponse;

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
    public void setup()
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
    }

    @Test
    public void testProcess()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        FusionController fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    protected FusionController createTestFusionController(String fusionSchema)
            throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException,
            InvocationTargetException, IllegalAccessException
    {
        cfg = spy(helper
                .readFusionSchemaWithoutValidation(fusionSchema));
        when(testRenderer.getResponseString(any(ClosableIterator.class), anyString())).thenReturn("<xml>42</xml>");
        when(cfg.getResponseRendererByType(any(ResponseRendererType.class))).thenReturn(testRenderer);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        if (searchServerConfigs != null && !searchServerConfigs.isEmpty())
        {
            SearchServerConfig searchServerConfig = spy(searchServerConfigs.get(0));
            searchServerConfigs.clear();
            searchServerConfigs.add(searchServerConfig);
            when(searchServerConfig.getInstance()).thenReturn(testAdapter);
            when(testResponse.getExtraInfo()).thenReturn(new SearchServerResponseInfo());
            when(testAdapter.sendQuery(Mockito.anyString())).thenReturn(testResponse);
        }
        return new FusionController(cfg);
    }

    @Test
    public void testWrongRenderer()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = spy(helper
                .readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
        when(testRenderer.getResponseString(any(ClosableIterator.class), anyString())).thenReturn("<xml>42</xml>");
        SearchServerConfig configuredSearchServer = spy(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0));
        cfg.getSearchServerConfigs().getSearchServerConfigs().clear();
        cfg.getSearchServerConfigs().getSearchServerConfigs().add(configuredSearchServer);
        when(configuredSearchServer.getInstance()).thenReturn(testAdapter);
        when(testAdapter.sendQuery(Mockito.anyString())).thenReturn(testResponse);
        FusionController fc = new FusionController(cfg);
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        // response format not set
        fusionRequest.setResponseType(null);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for not specified response type", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "Found no configuration for response renderer: <unknown>", fusionResponse.getErrorMessage());

        // renderer specified, but not configured
        cfg.getSearchServerConfigs().getResponseRendererFactories().clear();
        fusionRequest.setResponseType(ResponseRendererType.JSON);
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for unknown response type", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "Found no configuration for response renderer: JSON", fusionResponse.getErrorMessage());
    }

    @Test
    public void testTooLessResponses()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        FusionController fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for too less server responses", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", cfg.getDisasterMessage().getText(), fusionResponse.getErrorMessage());
    }

    @Test
    public void testSearchServersConfigured()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        FusionController fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for no servers configured", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "No search server configured at all.", fusionResponse.getErrorMessage());
    }

    @Test
    public void testQueryParsingFailed()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        FusionController fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:*:Schiller");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for bad query", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "Query parsing failed.", fusionResponse.getErrorMessage());
    }

    @Test
    public void testQueryWithMultipleResponseDocuments()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        DefaultResponseParser parser9000 = helper.getXmlUtil().unmarshal(DefaultResponseParser.class, "test-xml-response-9000.xml", null);
        List<Document> documents9000 = parser9000.getResult().getDocuments();
        ClosableListIterator<Document,SearchServerResponseInfo> documentsIt9000 = new ClosableListIterator<>(documents9000);
        SearchServerResponseInfo info = new SearchServerResponseInfo();
        info.setTotalNumberOfHits(parser9000.getResult().getNumFound());
        documentsIt9000.setExtraInfo(info);

        DefaultResponseParser parser9002 = helper.getXmlUtil().unmarshal(DefaultResponseParser.class, "test-xml-response-9002.xml", null);
        List<Document> documents9002 = parser9002.getResult().getDocuments();
        ClosableListIterator<Document,SearchServerResponseInfo> documentsIt9002 = new ClosableListIterator<>(documents9002);
        info = new SearchServerResponseInfo();
        info.setTotalNumberOfHits(parser9002.getResult().getNumFound());
        documentsIt9002.setExtraInfo(info);

        cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();

        SearchServerConfig searchServerConfig9000 = spy(searchServerConfigs.get(0));
        SearchServerConfig searchServerConfig9002 = spy(searchServerConfigs.get(1));

        searchServerConfigs.clear();

        searchServerConfigs.add(searchServerConfig9000);
        SearchServerAdapterIfc testAdapter9000 = spy(searchServerConfig9000.getInstance());
        when(searchServerConfig9000.getInstance()).thenReturn(testAdapter9000);
        when(testAdapter9000.sendQuery(Mockito.anyString())).thenReturn(documentsIt9000);

        searchServerConfigs.add(searchServerConfig9002);
        SearchServerAdapterIfc testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        when(testAdapter9002.sendQuery(Mockito.anyString())).thenReturn(documentsIt9002);

        FusionController fc = new FusionController(spyCfg);
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("title:abc");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
        // System.out.println("RESPONSE " + fusionResponse.getResponseAsString());
    }
}
