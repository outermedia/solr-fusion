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
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ControllerHighlightTest extends AbstractControllerTest
{
    @Test
    public void testProcess()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen"));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow")));
        fusionRequest.setSortSpec(new SortSpec("score", null, false));
        fusionRequest.setHighlight(new SolrFusionRequestParam("true"));
        fusionRequest.setHighlightQuery(new SolrFusionRequestParam("title:goethe"));
        fusionRequest.setHighlightPre(new SolrFusionRequestParam("pre"));
        fusionRequest.setHighlightPost(new SolrFusionRequestParam("post"));
        fusionRequest.setHighlightingFieldsToReturn(new SolrFusionRequestParam("title"));
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
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
        fusionRequest.setHighlightQuery(new SolrFusionRequestParam("author:*:Schiller"));

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
            "target/test-classes/test-xml-response-9002.xml", ResponseRendererType.XML,
            "test-fusion-schema-9000-9002.xml", 0, 1);
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title:def", "title", "xml"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(
            buildParams("titleVT_eng:abc", "titleVT_eng:def", "titleVT_eng", "xml"), 4000, "3.6");
    }

    protected Multimap<String> buildParams(String q, String hlq, String mappedTitle, String responseFormat)
    {
        Multimap<String> result = super.buildParams(q, null);
        result.put(HIGHLIGHT_QUERY, hlq);
        result.put(HIGHLIGHT_FIELDS_TO_RETURN, mappedTitle);
        result.set(FIELDS_TO_RETURN, "* score " + mappedTitle + " id");
        result.put(HIGHLIGHT, "true");
        result.put(HIGHLIGHT_PRE, "pre");
        result.put(HIGHLIGHT_POST, "post");
        result.set(WRITER_TYPE, responseFormat);
        return result;
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsXml()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:def", "target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", ResponseRendererType.XML,
            "test-fusion-schema-9000-9002.xml", 0, 1);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"hl\"><![CDATA[true]]></str>\n" +
            "    <str name=\"hl.simple.pre\"><![CDATA[pre]]></str>\n" +
            "    <str name=\"hl.simple.post\"><![CDATA[post]]></str>\n" +
            "    <str name=\"hl.fl\"><![CDATA[title]]></str>\n" +
            "    <str name=\"hl.q\"><![CDATA[title:def]]></str>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title:def", "title", "xml"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(
            buildParams("titleVT_eng:abc", "titleVT_eng:def", "titleVT_eng", "xml"), 4000, "3.6");
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsJson()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:def", "target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", ResponseRendererType.JSON,
            "test-fusion-schema-9000-9002.xml", 0, 1);
        String expected = "{\n" +
            "  \"responseHeader\":{\n" +
            "    \"status\":0,\n" +
            "    \"QTime\":0,\n" +
            "    \"params\":{\n" +
            "      \"indent\":\"on\",\n" +
            "      \"rows\":\"0\",\n" +
            "      \"q\":\"title:abc\",\n" +
            "      \"hl\":\"true\",\n" +
            "      \"hl.simple.pre\":\"pre\",\n" +
            "      \"hl.simple.post\":\"post\",\n" +
            "      \"hl.fl\":\"title\",\n" +
            "      \"hl.q\":\"title:def\",\n" +
            "      \"wt\":\"json\",\n" +
            "      \"version\":\"2.2\"}}\n" +
            "  , \"response\":{\"numFound\":0,\"start\":0,\"docs\":[\n" +
            "  ]}\n" +
            "}";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title:def", "title", "xml"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(
            buildParams("titleVT_eng:abc", "titleVT_eng:def", "titleVT_eng", "xml"), 4000, "3.6");
    }

    protected String testMultipleServers(String queryStr, String highlightQueryStr, String responseServer1,
        String responseServer2, ResponseRendererType format, String fusionSchema, int indexServer1, int indexServer2)
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        byte[] documents9000 = Files.toByteArray(new File(responseServer1));
        byte[] documents9002 = Files.toByteArray(new File(responseServer2));
        ByteArrayInputStream documents9000Stream = new ByteArrayInputStream(documents9000);
        ByteArrayInputStream documents9002Stream = new ByteArrayInputStream(documents9002);

        cfg = helper.readFusionSchemaWithoutValidation(fusionSchema);
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        List<SearchServerConfig> searchServerConfigs = spyCfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig searchServerConfig9000 = spy(searchServerConfigs.get(indexServer1));
        SearchServerConfig searchServerConfig9002 = spy(searchServerConfigs.get(indexServer2));
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
        fusionRequest.setResponseType(format);
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setHighlight(new SolrFusionRequestParam("true"));
        fusionRequest.setHighlightingFieldsToReturn(new SolrFusionRequestParam("title"));
        fusionRequest.setHighlightQuery(new SolrFusionRequestParam(highlightQueryStr));
        fusionRequest.setHighlightPre(new SolrFusionRequestParam("pre"));
        fusionRequest.setHighlightPost(new SolrFusionRequestParam("post"));
        // fusionRequest.setPageSize(10);
        // fusionRequest.setStart(0);
        fusionRequest.setSortSpec(new SortSpec(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE, null, false));
        FusionResponse fusionResponse = spy(new FusionResponse());
        doReturn(0l).when(fusionResponse).getQueryTime();
        fc.process(spyCfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());

        String result = fusionResponse.getResponseAsString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        // System.out.println("RESPONSE " + result);
        return result;
    }

    @Test
    public void testAddInHighlightResponse()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:def", "target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", ResponseRendererType.XML, "fusion-schema-uni-leipzig.xml", 0,
            1);
        // System.out.println("XML " + xml);
        String expectedHighlight = "<arr name=\"author_facet\">\n" +
            "        <str>{{{{START_HILITE}}}}Schiller{{{{END_HILITE}}}}, Friedrich</str>\n" +
            "        <str>Goethe- und {{{{START_HILITE}}}}Schiller{{{{END_HILITE}}}}-Archiv</str>\n" +
            "                </arr>";
        Assert.assertTrue("Didn't find expected highlight", xml.contains(expectedHighlight));
    }
}
