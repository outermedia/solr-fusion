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
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;
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

    Configuration cfg;

    @Mock
    private SearchServerConfig testSearchConfig;

    public static class TestRenderer implements ResponseRendererIfc
    {

        @Override
        public String getResponseString(ClosableIterator<Document,SearchServerResponseInfo> docStream, String query)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sb.append("<response>\n");
            sb.append("<lst name=\"responseHeader\">\n");
            sb.append("  <int name=\"status\">0</int>\n");
            sb.append("  <int name=\"QTime\">0</int>\n");
            sb.append("  <lst name=\"params\">\n");
            sb.append("    <str name=\"indent\">on</str>\n");
            sb.append("    <str name=\"start\">0</str>\n");
            sb.append("    <str name=\"q\"><![CDATA[" + query + "]]></str>\n");
            sb.append("    <str name=\"version\">2.2</str>\n");
            sb.append("    <str name=\"rows\">" + docStream.size() + "</str>\n");
            sb.append("  </lst>\n");
            sb.append("</lst>\n");
            int totalHitNumber = docStream.getExtraInfo().getTotalNumberOfHits();
            sb.append("<result name=\"response\" numFound=\"" + totalHitNumber + "\" start=\"0\">\n");
            Document d;
            FieldVisitor xmlVistor = new FieldVisitor()
            {
                @Override
                public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
                {
                    Term t = sf.getTerm();
                    writeTerm("    ", t.isWasMapped(), t.isRemoved(), t.getFusionFieldName(), t.getFusionFieldValue());
                    return true;
                }

                private void writeTerm(String indent, boolean wasMapped, boolean wasRemoved, String fusionFieldName, String fusionValue)
                {
                    if (wasMapped && !wasRemoved)
                    {
                        String typeTag = "str"; // TODO get from fusion schema!
                        sb.append(indent);
                        sb.append("<");
                        sb.append(typeTag);
                        if (fusionFieldName != null)
                        {
                            sb.append(" name=\"");
                            sb.append(fusionFieldName);
                            sb.append("\"");
                        }
                        sb.append("><![CDATA[");
                        sb.append("]]>");
                        sb.append("</");
                        sb.append(typeTag);
                        sb.append(">\n");
                    }
                    // TODO sf.getTerm().getNewTerms();
                }

                @Override
                public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
                {
                    List<Term> terms = msf.getTerms();
                    if (terms != null && !terms.isEmpty())
                    {
                        boolean printNone = true;
                        for (Term t : terms)
                        {
                            if (t.isWasMapped() && !t.isRemoved())
                            {
                                printNone = false;
                                break;
                            }
                        }
                        if (!printNone)
                        {
                            String fusionFieldName = terms.get(0).getFusionFieldName();
                            sb.append("    <arr name=\"" + fusionFieldName + "\">\n");
                            for (Term t : terms)
                            {
                                writeTerm("      ", t.isWasMapped(), t.isRemoved(), null, t.getFusionFieldValue());
                            }
                            sb.append("    </arr>\n");
                        }
                    }
                    return true;
                }
            };

            while (docStream.hasNext())
            {
                sb.append("  <doc>\n");
                d = docStream.next();
                d.accept(xmlVistor, null);
                sb.append("  </doc>\n");
            }
            sb.append("</response>\n");
            return sb + "\n";
        }

        @Override
        public void init(ResponseRendererFactory config)
        {
            // NOP
        }

        public static TestRenderer getInstance()
        {
            return new TestRenderer();
        }
    }

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
        DefaultResponseParser parser = helper.getXmlUtil().unmarshal(DefaultResponseParser.class, "test-xml-response-9000.xml", null);
        List<Document> documents = parser.getResult().getDocuments();
        ClosableListIterator<Document,SearchServerResponseInfo> documentsIt = new ClosableListIterator<>(documents);
        documentsIt.setExtraInfo(new SearchServerResponseInfo());
        cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema-9000.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig searchServerConfig = spy(searchServerConfigs.get(0));
        searchServerConfigs.clear();
        searchServerConfigs.add(searchServerConfig);
        SearchServerAdapterIfc testAdapter = spy(searchServerConfig.getInstance());
        when(searchServerConfig.getInstance()).thenReturn(testAdapter);
        when(testAdapter.sendQuery(Mockito.anyString())).thenReturn(documentsIt);
        FusionController fc = new FusionController(spyCfg);
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("title:abc");
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
        System.out.println("RESPONSE " + fusionResponse.getResponseAsString());
    }
}
