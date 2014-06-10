package org.outermedia.solrfusion;

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
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Created by ballmann on 6/6/14.
 */
public class ControllerTest
{
    protected TestHelper helper;

    @Mock
    ResponseRendererIfc testRenderer;

    @Mock
    ClosableIterator<Document> testResponse;

    Configuration cfg;

    @Before
    public void setup()
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
    }

    @Test
    public void testProcess() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        FusionController fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    protected FusionController createTestFusionController(String fusionSchema) throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        cfg = spy(helper
                .readFusionSchemaWithoutValidation(fusionSchema));
        doReturn("<xml>42</xml>").when(testRenderer).getResponseString(any(ClosableIterator.class));
        doReturn(testRenderer).when(cfg).getResponseRendererByType(any(ResponseRendererType.class));
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        if (searchServerConfigs != null && !searchServerConfigs.isEmpty())
        {
            SearchServerConfig searchServerConfig = searchServerConfigs.get(0);
            SearchServerAdapterIfc testAdapter = spy(searchServerConfig.getImplementation());
            searchServerConfig.setImplementation(testAdapter);
            doReturn(testResponse).when(testAdapter).sendQuery(Mockito.anyString());
        }
        return new FusionController(cfg);
    }

    @Test
    public void testWrongRenderer() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = spy(helper
                .readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
        doReturn("<xml>42</xml>").when(testRenderer).getResponseString(any(ClosableIterator.class));
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        SearchServerAdapterIfc testAdapter = spy(searchServerConfig.getImplementation());
        searchServerConfig.setImplementation(testAdapter);
        doReturn(testResponse).when(testAdapter).sendQuery(Mockito.anyString());
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
    public void testTooLessResponses() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
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
    public void testSearchServersConfigured() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
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
    public void testQueryParsingFailed() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        FusionController fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:*:Schiller");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error bad query", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "Query parsing failed.", fusionResponse.getErrorMessage());
    }
}
