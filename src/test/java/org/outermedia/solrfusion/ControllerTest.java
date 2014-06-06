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

    @Before
    public void setup()
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcess() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = spy(helper
                .readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
        doReturn("<xml>42</xml>").when(testRenderer).getResponseString(any(ClosableIterator.class));
        doReturn(testRenderer).when(cfg).getResponseRendererByType(any(ResponseRendererType.class));
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        SearchServerAdapterIfc testAdapter = spy(searchServerConfig.getImplementation());
        searchServerConfig.setImplementation(testAdapter);
        doReturn(testResponse).when(testAdapter).sendQuery(Mockito.anyString());
        FusionController fc = new FusionController(cfg);
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        System.out.println("R "+fusionResponse.getErrorMessage());
        Assert.assertTrue("", fusionResponse.isOk());
    }
}
