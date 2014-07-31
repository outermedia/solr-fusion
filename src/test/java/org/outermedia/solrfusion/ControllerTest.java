package org.outermedia.solrfusion;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.NumericRangeQuery;
import org.outermedia.solrfusion.query.parser.PhraseQuery;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyMapOf;
import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ControllerTest
{
    TestHelper helper;

    @Mock ResponseRendererIfc testRenderer;

    ByteArrayInputStream testResponse;

    @Mock SearchServerAdapterIfc testAdapter;

    @Mock SearchServerAdapterIfc testAdapter9000;

    @Mock SearchServerAdapterIfc testAdapter9002;

    Configuration cfg;

    @Mock
    private SearchServerConfig testSearchConfig;

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

    @Test
    public void testProcess()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
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
            when(testAdapter.sendQuery(Mockito.anyMapOf(String.class, String.class), Mockito.anyInt())).thenReturn(
                testResponse);
        }
        return cfg.getController();
    }

    @Test
    public void testWrongRenderer()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        Configuration cfg = spy(helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
        when(testRenderer.getResponseString(any(Configuration.class), any(ClosableIterator.class),
            any(FusionRequest.class), any(FusionResponse.class))).thenReturn("<xml>42</xml>");
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig configuredSearchServer = spy(searchServerConfigs.get(0));
        searchServerConfigs.clear();
        searchServerConfigs.add(configuredSearchServer);
        when(configuredSearchServer.getInstance()).thenReturn(testAdapter);
        when(testAdapter.sendQuery(anyMapOf(String.class, String.class), Mockito.anyInt())).thenReturn(testResponse);
        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
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
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for too less server responses", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", cfg.getDisasterMessage().getText(),
            fusionResponse.getErrorMessage().trim());
    }

    @Test
    public void testSearchServersConfigured()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:Schiller -title:morgen");
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for no servers configured", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected", "No search server configured at all.",
            fusionResponse.getErrorMessage());
    }

    @Test
    public void testQueryParsingFailed()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery("author:*:Schiller");
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for bad query", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected",
            "Query parsing failed: author:*:Schiller\nCause: ERROR: Parsing of query author:*:Schiller failed.\n" +
                "Cannot interpret query 'author:*:Schiller': '*' or '?' not allowed as first character in WildcardQuery\n" +
                "'*' or '?' not allowed as first character in WildcardQuery", fusionResponse.getErrorMessage().trim());
    }

    @Test
    public void testQueryWithMultipleServersAndResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        testMultipleServers("target/test-classes/test-xml-response-9000.xml",
            "target/test-classes/test-xml-response-9002.xml");
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc"), 4000);
        verify(testAdapter9002, times(1)).sendQuery(buildParams("titleVT_eng:abc"), 4000);
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
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
            "    <str name=\"sort\"><![CDATA[score]]></str>\n" +
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
        result.put(QUERY.getRequestParamName(), q);
        result.put(PAGE_SIZE.getRequestParamName(), "10");
        result.put(START.getRequestParamName(), "0");
        result.put(SORT.getRequestParamName(), "score desc");
        result.put(FIELDS_TO_RETURN.getRequestParamName(), "* score");
        return result;
    }

    protected String testMultipleServers(String responseServer1, String responseServer2)
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

    @Test
    public void testBuildParams()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        FusionController controller = (FusionController) FusionController.Factory.getInstance();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        controller.configuration = cfg;
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName("Bibliothek9002");
        Assert.assertNotNull("Didn't find server 9002", serverConfig);
        FusionRequest fusionRequest = new FusionRequest();
        Term t = Term.newFusionTerm("title", "abc");
        t.setSearchServerFieldName("titleVT_de");
        t.setSearchServerFieldValue(Arrays.asList("abc"));
        t.setWasMapped(true);
        t.setRemoved(false);
        TermQuery q = new TermQuery(t);
        fusionRequest.setParsedQuery(q);
        fusionRequest.setStart(5);
        fusionRequest.setPageSize(200);
        fusionRequest.setSolrFusionSortField("title");
        fusionRequest.setSortAsc(true);
        Map<String, String> map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other sort field", "titleVT_de asc", map.get(SORT.getRequestParamName()));
        Assert.assertEquals("Expected other start value", "0", map.get(START.getRequestParamName()));
        int maxDocs = serverConfig.getMaxDocs();
        Assert.assertEquals("Expected other page size", String.valueOf(maxDocs),
            map.get(PAGE_SIZE.getRequestParamName()));
        Assert.assertEquals("Expected other start value", "titleVT_de:abc", map.get(QUERY.getRequestParamName()));

        // below server's max limit, return wanted size
        int start = 4;
        Assert.assertTrue("Please set max-docs >" + start, start < maxDocs);
        fusionRequest.setStart(start);
        fusionRequest.setPageSize(maxDocs - 1 - start);
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", "99", map.get(PAGE_SIZE.getRequestParamName()));

        // up to server's max limit, return wanted size
        fusionRequest.setStart(start);
        fusionRequest.setPageSize(maxDocs - start);
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", String.valueOf(maxDocs),
            map.get(PAGE_SIZE.getRequestParamName()));

        // above  server's max limit, return server's limit
        fusionRequest.setStart(start + 1);
        fusionRequest.setPageSize(maxDocs - start);
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", String.valueOf(maxDocs),
            map.get(PAGE_SIZE.getRequestParamName()));
    }

    @Test
    public void testMapFusionFieldToSearchServerField()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        FusionController controller = (FusionController) FusionController.Factory.getInstance();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        controller.configuration = cfg;
        SearchServerConfig serverConfig = cfg.getSearchServerConfigByName("Bibliothek9002");
        Assert.assertNotNull("Didn't find server 9002", serverConfig);

        FusionRequest request = new FusionRequest();

        String searchServerField = mapField("title", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "titleVT_de", searchServerField);

        searchServerField = mapField("language_de", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "language", searchServerField);

        searchServerField = mapField("unknown", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "score", searchServerField);

        // special case id
        searchServerField = mapField("id", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "id", searchServerField);

        // special case score
        searchServerField = mapField("score", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "score", searchServerField);

        // language_de and language_en are both mapped to language
        String fl = request.mapFusionFieldListToSearchServerField("language_de, language_en", cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "language", fl);

        // title is mapped to two fields, preserve order of textual order of mappings
        fl = request.mapFusionFieldListToSearchServerField("title id", cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "titleVT_de titleVT_eng id", fl);
    }

    protected String mapField(String field, FusionRequest request, Configuration cfg, SearchServerConfig serverConfig)
        throws InvocationTargetException, IllegalAccessException
    {
        return request.mapFusionFieldToSearchServerField(field, cfg, serverConfig).iterator().next();
    }

    @Test
    public void testIsIdQuery() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        FusionController controller = (FusionController) FusionController.Factory.getInstance();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        controller.configuration = cfg;
        IdGeneratorIfc idGenerator = cfg.getIdGenerator();
        String fusionIdField = idGenerator.getFusionIdField();

        PhraseQuery pq = new PhraseQuery(
            Term.newFusionTerm(fusionIdField, idGenerator.computeId("Bibliothek9000", "v1")));
        Assert.assertTrue("Phrase query should an id query", controller.isIdQuery(pq));

        pq = new PhraseQuery(Term.newFusionTerm(fusionIdField, idGenerator.computeId("unknownserver", "v1")));
        Assert.assertFalse("Phrase query with unknown server shouldn't be an id query", controller.isIdQuery(pq));

        pq = new PhraseQuery(Term.newFusionTerm(fusionIdField, "v1"));
        Assert.assertFalse("Phrase query with wrong id value shouldn't be an id query", controller.isIdQuery(pq));

        pq = new PhraseQuery(Term.newFusionTerm("xid", idGenerator.computeId("Bibliothek9000", "v1")));
        Assert.assertFalse("Phrase query with non id field shouldn't be an id query", controller.isIdQuery(pq));

        NumericRangeQuery rq = NumericRangeQuery.newLongRange(fusionIdField, 1L, 10L, true, true);
        Assert.assertFalse("Non term query shouldn't be an id query", controller.isIdQuery(rq));
    }

    @Test
    public void testRequestOneSearchServer()
        throws InvocationTargetException, IllegalAccessException, FileNotFoundException, ParserConfigurationException,
        SAXException, JAXBException
    {
        FusionController controller = spy((FusionController) FusionController.Factory.getInstance());
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        controller.configuration = cfg;
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigByName("Bibliothek9000");
        ResponseParserIfc responseParser = searchServerConfig.getResponseParser(cfg.getDefaultResponseParser());
        String xmlResponseStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"q\"><![CDATA[id:...]]></str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "    <str name=\"rows\">2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"1\" start=\"0\">\n" +
            "  <doc>\n" +
            "    <str name=\"id\"><![CDATA[1]]></str>\n" +
            "    <str name=\"title\"><![CDATA[abc]]></str>\n" +
            "    <float name=\"score\"><![CDATA[0.6750762040000001]]></float>\n" +
            "  </doc>\n" +
            "</result>\n" +
            "</response>";
        XmlResponse xmlResponse = responseParser.parse(new StringBufferInputStream(xmlResponseStr));
        doReturn(xmlResponse).when(controller).sendAndReceive(any(FusionRequest.class), any(SearchServerConfig.class));
        FusionRequest request = new FusionRequest();
        String sep = DefaultIdGenerator.SEPARATOR;
        request.setQuery("id:\"Bibliothek9000" + sep + "1\"");
        request.setResponseType(ResponseRendererType.XML);
        request.setParsedQuery(controller.parseQuery(request.getQuery(), null, Locale.GERMAN, request));
        FusionResponse response = new FusionResponse();

        log.info("--- without merger test ---");
        controller.processIdQuery(request, response);
        Assert.assertTrue("Expected no error", response.isOk());
        String expected = "  <doc>\n" +
            "    <str name=\"id\"><![CDATA[Bibliothek9000" + sep + "1]]></str>\n" +
            "    <str name=\"title\"><![CDATA[abc]]></str>\n" +
            "    <float name=\"score\"><![CDATA[0.8100914448000002]]></float>\n" +
            "  </doc>";
        Assert.assertTrue("Response doesn't contain doc: " + response.getResponseAsString(),
            response.getResponseAsString().contains(expected));

        // with document merger
        log.info("--- with merger test ---");
        TestMerger merger = new TestMerger();
        merger.setFusionName("isbn");
        merger.setClassFactory("org.outermedia.solrfusion.DefaultMergeStrategy$Factory");
        List<MergeTarget> targets = new ArrayList<>();
        MergeTarget mt1 = new MergeTarget();
        mt1.setPrio(1);
        mt1.setTargetName("Bibliothek9000");
        targets.add(mt1);
        MergeTarget mt2 = new MergeTarget();
        mt2.setPrio(2);
        mt2.setTargetName("Bibliothek9002");
        targets.add(mt2);
        merger.setTargets(targets);
        merger.afterUnmarshal(null, null);
        cfg.getSearchServerConfigs().setMerge(merger);
        xmlResponse = responseParser.parse(new StringBufferInputStream(xmlResponseStr));
        doReturn(xmlResponse).when(controller).sendAndReceive(any(FusionRequest.class), any(SearchServerConfig.class));
        response = new FusionResponse();
        // reset modified query object
        request.setParsedQuery(controller.parseQuery(request.getQuery(), null, Locale.GERMAN, request));
        controller.processIdQuery(request, response);
        Assert.assertTrue("Expected no error", response.isOk());
        Assert.assertTrue("Response doesn't contain doc: " + response.getResponseAsString(),
            response.getResponseAsString().contains(expected));

        // exception occurred
        log.info("--- exception test ---");
        xmlResponse.setErrorReason(new RuntimeException("Error1"));
        response = new FusionResponse();
        // reset modified query object
        request.setParsedQuery(controller.parseQuery(request.getQuery(), null, Locale.GERMAN, request));
        controller.processIdQuery(request, response);
        Assert.assertEquals("Expected error", "Internal processing error. Reason: Error1", response.getErrorMessage());

        // vufind id query after click on a book
        log.info("--- vufind test without merger ---");
        String xmlResponsWithoutIdStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"start\">0</str>\n" +
            "    <str name=\"q\"><![CDATA[id:...]]></str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "    <str name=\"rows\">2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"1\" start=\"0\">\n" +
            "  <doc>\n" +
            "    <str name=\"title\"><![CDATA[abc]]></str>\n" +
            "    <float name=\"score\"><![CDATA[0.6750762040000001]]></float>\n" +
            "  </doc>\n" +
            "</result>\n" +
            "</response>";

        cfg.getSearchServerConfigs().setMerge(null);
        xmlResponse = responseParser.parse(new StringBufferInputStream(xmlResponsWithoutIdStr));
        doReturn(xmlResponse).when(controller).sendAndReceive(any(FusionRequest.class), any(SearchServerConfig.class));
        response = new FusionResponse();
        request.setQuery("id:Bibliothek9000" + sep + "1");
        request.setParsedQuery(controller.parseQuery(request.getQuery(), null, Locale.GERMAN, request));
        request.setFilterQuery("");
        request.setParsedFilterQuery(controller.parseQuery(request.getFilterQuery(), null, Locale.GERMAN, request));
        request.setResponseType(ResponseRendererType.XML);
        controller.process(cfg, request, response);
        Assert.assertTrue("Expected no error: " + response.getErrorMessage(), response.isOk());
        log.debug("Returning:\n{}", response.getResponseAsString());

        log.info("--- vufind test with merger ---");
        cfg.getSearchServerConfigs().setMerge(merger);
        xmlResponse = responseParser.parse(new StringBufferInputStream(xmlResponsWithoutIdStr));
        doReturn(xmlResponse).when(controller).sendAndReceive(any(FusionRequest.class), any(SearchServerConfig.class));
        response = new FusionResponse();
        request.setParsedQuery(controller.parseQuery(request.getQuery(), null, Locale.GERMAN, request));
        controller.process(cfg, request, response);
        Assert.assertTrue("Expected no error: " + response.getErrorMessage(), response.isOk());
        log.debug("Returning:\n{}", response.getResponseAsString());
    }
}
