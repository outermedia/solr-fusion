package org.outermedia.solrfusion;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;
import org.outermedia.solrfusion.adapter.solr.SolrFusionUriBuilder;
import org.outermedia.solrfusion.adapter.solr.Version;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ControllerTest extends AbstractControllerTest
{

    private FusionRequest fusionRequest;
    private Configuration spyCfg;
    private SearchServerConfig searchServerConfig9000;
    private SearchServerConfig searchServerConfig9002;

    @Test
    public void testProcess()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen", null));
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    @Test
    public void testWrongRenderer()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        Configuration cfg = spy(helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml"));
//        when(testRenderer.writeResponse(any(Configuration.class), any(ClosableIterator.class), any(FusionRequest.class),
//            any(FusionResponse.class))).thenReturn("<xml>42</xml>");
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        SearchServerConfig configuredSearchServer = spy(searchServerConfigs.get(0));
        searchServerConfigs.clear();
        searchServerConfigs.add(configuredSearchServer);
        when(configuredSearchServer.getInstance()).thenReturn(testAdapter);
        SolrFusionUriBuilderIfc testParams = mock(SolrFusionUriBuilderIfc.class);
        when(testAdapter.buildHttpClientParams(any(Configuration.class), any(SearchServerConfig.class),
            any(FusionRequest.class), any(Multimap.class), any(Version.class))).thenReturn(testParams);
        when(testAdapter.sendQuery(any(SolrFusionUriBuilder.class), Mockito.anyInt())).thenReturn(testResponse);
        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen", null));
        // response format not set
        fusionRequest.setResponseType(null);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertFalse("Expected processing error for not specified response type", fusionResponse.isOk());
        Assert.assertEquals("Found different error message than expected",
            "Found no configuration for response renderer: <unknown>", fusionResponse.getErrorMessage());

        // first fc.process() consumed test response, so re-initConsolidator it and bind the new object to the testAdapter again
        initTestResponse();
        when(testAdapter.sendQuery(any(SolrFusionUriBuilder.class), Mockito.anyInt())).thenReturn(testResponse);
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
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen", null));
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
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen", null));
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
        fusionRequest.setQuery(new SolrFusionRequestParam("author:*:Schiller", null));
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
        testMultipleServers("target/test-classes/test-xml-response-9000.xml",
            "target/test-classes/test-xml-response-9002.xml");
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildParams("title:abc", null), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildParams("titleVT_eng:abc", null), new Version("3.6"));
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
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"wt\">xml</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildParams("title:abc", null), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildParams("titleVT_eng:abc", null), new Version("3.6"));
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
        spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        List<SearchServerConfig> searchServerConfigs = spyCfg.getSearchServerConfigs().getSearchServerConfigs();
        searchServerConfig9000 = spy(searchServerConfigs.get(0));
        searchServerConfig9002 = spy(searchServerConfigs.get(1));
        searchServerConfigs.clear();

        searchServerConfigs.add(searchServerConfig9000);
        testAdapter9000 = spy(searchServerConfig9000.getInstance());
        when(searchServerConfig9000.getInstance()).thenReturn(testAdapter9000);
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(any(SolrFusionUriBuilder.class),
            Mockito.anyInt());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(any(SolrFusionUriBuilder.class),
            Mockito.anyInt());

        FusionControllerIfc fc = cfg.getController();
        fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("title:abc", null));
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = new FusionResponse();
        StringWriter sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        FusionResponse fusionResponseSpy = spy(fusionResponse);
        doReturn(0l).when(fusionResponseSpy).getQueryTime();
        fc.process(spyCfg, fusionRequest, fusionResponseSpy);
        Assert.assertTrue("Expected no processing error", fusionResponseSpy.isOk());

        String result = sw.toString();
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
        fusionRequest.setFieldsToReturn(new SolrFusionRequestParam("id,title", null));
        fusionRequest.setQueryType(new SolrFusionRequestParam("morelikethis", null));
        fusionRequest.setResponseType(ResponseRendererType.JSON);
        fusionRequest.setSort(new SolrFusionRequestParam("title asc"));
        Term hlt = Term.newFusionTerm("title", "abc");
        hlt.setSearchServerFieldName("titleVT_de");
        hlt.setSearchServerFieldValue(Arrays.asList("abc"));
        hlt.setWasMapped(true);
        hlt.setRemoved(false);
        TermQuery hlq = new TermQuery(hlt);
        fusionRequest.setParsedHighlightQuery(hlq);
        fusionRequest.setHighlight(new SolrFusionRequestParam("true", null));
        fusionRequest.setHighlightPre(new SolrFusionRequestParam("pre", null));
        fusionRequest.setHighlightPost(new SolrFusionRequestParam("post", null));
        fusionRequest.setHighlightingFieldsToReturn(new SolrFusionRequestParam("title,id", null));
        fusionRequest.setFacet(new SolrFusionRequestParam("true", null));
        fusionRequest.setFacetSort(new SolrFusionRequestParam("index", null));
        fusionRequest.setFacetLimit(new SolrFusionRequestParam("20", null));
        fusionRequest.setFacetMincount(new SolrFusionRequestParam("2", null));
        fusionRequest.setFacetPrefix(new SolrFusionRequestParam("p1", null));
        List<SolrFusionRequestParam> facetFields = new ArrayList<>();
        facetFields.add(new SolrFusionRequestParam("{!ex=format_filter}title", null));
        facetFields.add(new SolrFusionRequestParam("{!ex=format_de15_filter}author", null));
        fusionRequest.setFacetFields(facetFields);
        List<SolrFusionRequestParam> facetSortFields = new ArrayList<>();
        facetSortFields.add(new SolrFusionRequestParam("index1", "title", null));
        facetSortFields.add(new SolrFusionRequestParam("index2", "author", null));
        fusionRequest.setFacetSortFields(facetSortFields);

        Multimap<String> map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other sort field", "titleVT_eng asc", map.getFirst(SORT));
        Assert.assertNull("Expected no start value", map.getFirst(START));
        int maxDocs = serverConfig.getMaxDocs();
        Assert.assertNull("Expected no page size", map.getFirst(PAGE_SIZE));
        Assert.assertEquals("Expected other search server query", "titleVT_de:abc", map.getFirst(QUERY));
        Assert.assertEquals("Expected other query type", "morelikethis", map.getFirst(QUERY_TYPE));
        Assert.assertEquals("Expected other response type", "xml", map.getFirst(WRITER_TYPE));

        // check highlights
        Assert.assertEquals("Expected other search server highlight query", "titleVT_de:abc",
            map.getFirst(HIGHLIGHT_QUERY));
        Assert.assertEquals("Expected other search server highlight pre", "pre", map.getFirst(HIGHLIGHT_PRE));
        Assert.assertEquals("Expected other search server highlight post", "post", map.getFirst(HIGHLIGHT_POST));
        Assert.assertEquals("Expected other search server highlight value", "true", map.getFirst(HIGHLIGHT));
        Assert.assertEquals("Expected other search server highlight fields", "titleVT_eng id",
            map.getFirst(HIGHLIGHT_FIELDS_TO_RETURN));

        // check facet params
        Assert.assertEquals("Expected other search server facet", "true", map.getFirst(FACET));
        Assert.assertEquals("Expected other search server facet prefix", "p1", map.getFirst(FACET_PREFIX));
        Assert.assertEquals("Expected other search server facet min", "2", map.getFirst(FACET_MINCOUNT));
        Assert.assertEquals("Expected other search server facet limit", "20", map.getFirst(FACET_LIMIT));
        Assert.assertEquals("Expected other search server facet sort", "index", map.getFirst(FACET_SORT));
        List<String> expected = Arrays.asList("{!ex=format_de15_filter}author9002", "{!ex=format_filter}titleVT_eng");
        Assert.assertEquals("Got different facet fields", expected, new ArrayList<>(map.get(FACET_FIELD)));
        List<Map.Entry<String, String>> collectedFacetSortFields = map.filterBy(FACET_SORT_FIELD);
        String actualStr = collectedFacetSortFields.toString();
        String expectedStr = "[f.titleVT_eng.facet.sort=index1, f.author9002.facet.sort=index2]";
        Assert.assertEquals("Got different facet sort fields", expectedStr, actualStr);

        // below server's max limit, return wanted size
        int start = 4;
        Assert.assertTrue("Please set max-docs >" + start, start < maxDocs);
        fusionRequest.setStart(new SolrFusionRequestParam(String.valueOf(start)));
        fusionRequest.setPageSize(new SolrFusionRequestParam(String.valueOf(maxDocs - 1 - start)));
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", "99", map.getFirst(PAGE_SIZE));

        // up to server's max limit, return wanted size
        fusionRequest.setStart(new SolrFusionRequestParam(String.valueOf(start)));
        fusionRequest.setPageSize(new SolrFusionRequestParam(String.valueOf(maxDocs - start)));
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", String.valueOf(maxDocs), map.getFirst(PAGE_SIZE));

        // above  server's max limit, return server's limit
        fusionRequest.setStart(new SolrFusionRequestParam(String.valueOf(start + 1)));
        fusionRequest.setPageSize(new SolrFusionRequestParam(String.valueOf(maxDocs - start)));
        map = fusionRequest.buildSearchServerQueryParams(cfg, serverConfig);
        Assert.assertEquals("Expected other page size", String.valueOf(maxDocs), map.getFirst(PAGE_SIZE));
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
        Assert.assertEquals("Mapping returned other field than expected", "titleVT_eng", searchServerField);

        searchServerField = mapField("language_de", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "language", searchServerField);

        searchServerField = mapField("unknown", request, cfg, serverConfig);
        Assert.assertNull("Mapping shouldn't find a mapping", searchServerField);

        // special case id
        searchServerField = mapField("id", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "id", searchServerField);

        // special case score
        searchServerField = mapField("score", request, cfg, serverConfig);
        Assert.assertEquals("Mapping returned other field than expected", "score", searchServerField);

        // language_de and language_en are both mapped to language
        String fl = request.mapFusionFieldListToSearchServerField("language_de, language_en", cfg, serverConfig, null,
            false, QueryTarget.HIGHLIGHT_QUERY);
        Assert.assertEquals("Mapping returned other field than expected", "language", fl);

        // title is mapped to two fields, preserve order of textual order of mappings
        fl = request.mapFusionFieldListToSearchServerField("title id", cfg, serverConfig, null, false,
            QueryTarget.HIGHLIGHT_QUERY);
        Assert.assertEquals("Mapping returned other field than expected", "titleVT_eng id", fl);
    }

    protected String mapField(String field, FusionRequest request, Configuration cfg, SearchServerConfig serverConfig)
        throws InvocationTargetException, IllegalAccessException
    {
        Set<String> strings = request.mapFusionFieldToSearchServerField(field, cfg, serverConfig, null,
            QueryTarget.ALL);
        if (strings == null || strings.isEmpty())
        {
            return null;
        }
        return strings.iterator().next();
    }

}
