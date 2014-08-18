package org.outermedia.solrfusion;

import com.google.common.io.Files;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Merge;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 8/6/14.
 */
public class AbstractControllerTest
{
    ByteArrayInputStream testResponse;
    TestHelper helper;
    @Mock ResponseRendererIfc testRenderer;
    @Mock SearchServerAdapterIfc testAdapter;
    Configuration cfg;
    @Mock SearchServerAdapterIfc testAdapter9000;

    @Mock SearchServerAdapterIfc testAdapter9002;

    @Mock
    private SearchServerConfig testSearchConfig;

    protected Multimap<String> buildParams(String q, String fq)
    {
        Multimap<String> result = new Multimap<>();
        result.put(QUERY, q);
        if (fq != null)
        {
            result.put(FILTER_QUERY, fq);
        }
        result.put(PAGE_SIZE, "10");
        result.put(START, "0");
        result.put(SORT, "score desc");
        result.put(FIELDS_TO_RETURN, "* score");
        result.put(WRITER_TYPE, "xml");
        return result;
    }

    static class TestMerger extends Merge
    {
        public void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
        {
            super.afterUnmarshal(u, parent);
        }
    }

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

    protected FusionControllerIfc createTestFusionController(String fusionSchema)
        throws IOException, JAXBException, SAXException, ParserConfigurationException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        cfg = spy(helper.readFusionSchemaWithoutValidation(fusionSchema));
        when(testRenderer.getResponseString(any(Configuration.class), any(ClosableIterator.class),
            any(FusionRequest.class), any(FusionResponse.class))).thenReturn("<xml>42</xml>");
        when(cfg.getResponseRendererByType(any(ResponseRendererType.class))).thenReturn(testRenderer);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        if (searchServerConfigs != null && !searchServerConfigs.isEmpty())
        {
            SearchServerConfig searchServerConfig = spy(searchServerConfigs.get(0));
            searchServerConfigs.clear();
            searchServerConfigs.add(searchServerConfig);
            when(searchServerConfig.getInstance()).thenReturn(testAdapter);
            when(testAdapter.sendQuery(Mockito.any(Multimap.class), Mockito.anyInt(), anyString())).thenReturn(
                testResponse);
        }
        return cfg.getController();
    }
}
