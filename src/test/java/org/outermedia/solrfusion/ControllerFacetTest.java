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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ControllerFacetTest extends AbstractControllerTest
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
        fusionRequest.setSortAsc(false);
        fusionRequest.setSolrFusionSortField("score");
        fusionRequest.setFacet(new SolrFusionRequestParam("true"));
        fusionRequest.setFacetPrefix(new SolrFusionRequestParam("p1"));
        fusionRequest.setFacetMincount(new SolrFusionRequestParam("2"));
        fusionRequest.setFacetLimit(new SolrFusionRequestParam("20"));
        fusionRequest.setFacetSort(new SolrFusionRequestParam("index"));
        List<SolrFusionRequestParam> facetFields = new ArrayList<>();
        facetFields.add(new SolrFusionRequestParam("{!ex=format_filter}format"));
        facetFields.add(new SolrFusionRequestParam("access_facet"));
        fusionRequest.setFacetFields(facetFields);
        List<SolrFusionRequestParam> facetSortFields = new ArrayList<>();
        facetSortFields.add(new SolrFusionRequestParam("index1", "finc_class_facet"));
        facetSortFields.add(new SolrFusionRequestParam("index2", "format"));
        fusionRequest.setFacetSortFields(facetSortFields);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    protected Multimap<String> buildParams(String q, String title1, String title2, String author)
    {
        Multimap<String> result = super.buildParams(q, null);
        result.set(FIELDS_TO_RETURN, "* score");
        result.put(FACET, "true");
        result.put(FACET_PREFIX, "p1");
        result.put(FACET_MINCOUNT, "2");
        result.put(FACET_LIMIT, "20");
        result.put(FACET_SORT, "index");
        result.put(FACET_FIELD, "{!ex=format_filter}" + title1);
        result.put(FACET_FIELD, "{!ex=format_filter}" + title2);
        result.put(FACET_FIELD, author);
        String facetSortField1 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(title1, Locale.GERMAN);
        result.put(facetSortField1, "index1");
        String facetSortField2 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(title2, Locale.GERMAN);
        result.put(facetSortField2, "index1");
        String facetSortField3 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(author, Locale.GERMAN);
        result.put(facetSortField3, "index2");
        return result;
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsXml()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", ResponseRendererType.XML,
            "test-fusion-schema-9000-9002.xml", null);
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
            "    <str name=\"facet\"><![CDATA[true]]></str>\n" +
            "    <str name=\"facet.limit\"><![CDATA[20]]></str>\n" +
            "    <str name=\"facet.mincount\"><![CDATA[2]]></str>\n" +
            "    <str name=\"facet.prefix\"><![CDATA[p1]]></str>\n" +
            "    <str name=\"facet.sort\"><![CDATA[index]]></str>\n" +
            "    <str name=\"f.title.facet.sort\"><![CDATA[index1]]></str>\n" +
            "    <str name=\"f.author.facet.sort\"><![CDATA[index2]]></str>\n" +
            "    <arr name=\"facet.field\">\n" +
            "        <str>{!ex=format_filter}title</str>\n" +
            "        <str>author</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title", "title", "author9000"), 4000);
        verify(testAdapter9002, times(1)).sendQuery(
            buildParams("titleVT_eng:abc", "titleVT_eng", "titleVT_de", "author9002"), 4000);
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsJson()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", ResponseRendererType.JSON,
            "test-fusion-schema-9000-9002.xml", null);
        String expected = "{\n" +
            "  \"responseHeader\":{\n" +
            "    \"status\":0,\n" +
            "    \"QTime\":0,\n" +
            "    \"params\":{\n" +
            "      \"indent\":\"on\",\n" +
            "      \"start\":\"0\",\n" +
            "      \"rows\":\"0\",\n" +
            "      \"q\":\"title:abc\",\n" +
            "      \"sort\":\"score\",\n" +
            "      \"facet\":\"true\",\n" +
            "      \"facet.limit\":\"20\",\n" +
            "      \"facet.mincount\":\"2\",\n" +
            "      \"facet.prefix\":\"p1\",\n" +
            "      \"facet.sort\":\"index\",\n" +
            "      \"f.title.facet.sort\":\"index1\",\n" +
            "      \"f.author.facet.sort\":\"index2\",\n" +
            "      \"facet.field\":[\n" +
            "        \"{!ex=format_filter}title\",\"author\"\n" +
            "      ],\n" +
            "      \"wt\":\"json\",\n" +
            "      \"version\":\"2.2\"}},\n" +
            "  \"response\":{\"numFound\":0,\"start\":0,\"docs\":[\n" +
            "  ]}\n" +
            "}";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(buildParams("title:abc", "title", "title", "author9000"), 4000);
        verify(testAdapter9002, times(1)).sendQuery(
            buildParams("titleVT_eng:abc", "titleVT_eng", "titleVT_de", "author9002"), 4000);
    }

    protected String testMultipleServers(String queryStr, String responseServer1, String responseServer2,
        ResponseRendererType format, String fusionSchemaPath, List<SolrFusionRequestParam> filterQueries)
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        byte[] documents9000 = Files.toByteArray(new File(responseServer1));
        byte[] documents9002 = Files.toByteArray(new File(responseServer2));
        ByteArrayInputStream documents9000Stream = new ByteArrayInputStream(documents9000);
        ByteArrayInputStream documents9002Stream = new ByteArrayInputStream(documents9002);

        cfg = helper.readFusionSchemaWithoutValidation(fusionSchemaPath);
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
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(any(Multimap.class), Mockito.anyInt());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(any(Multimap.class), Mockito.anyInt());

        FusionControllerIfc fc = cfg.getController();
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setResponseType(format);
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setFacet(new SolrFusionRequestParam("true"));
        fusionRequest.setFacetPrefix(new SolrFusionRequestParam("p1"));
        fusionRequest.setFacetMincount(new SolrFusionRequestParam("2"));
        fusionRequest.setFacetLimit(new SolrFusionRequestParam("20"));
        fusionRequest.setFacetSort(new SolrFusionRequestParam("index"));
        List<SolrFusionRequestParam> facetFields = new ArrayList<>();
        facetFields.add(new SolrFusionRequestParam("{!ex=format_filter}title"));
        facetFields.add(new SolrFusionRequestParam("author"));
        fusionRequest.setFacetFields(facetFields);
        List<SolrFusionRequestParam> facetSortFields = new ArrayList<>();
        facetSortFields.add(new SolrFusionRequestParam("index1", "title"));
        facetSortFields.add(new SolrFusionRequestParam("index2", "author"));
        fusionRequest.setFacetSortFields(facetSortFields);
        fusionRequest.setPageSize(10);
        fusionRequest.setStart(0);
        fusionRequest.setSortAsc(false);
        fusionRequest.setFilterQuery(filterQueries);
        fusionRequest.setSolrFusionSortField(ResponseMapperIfc.FUSION_FIELD_NAME_SCORE);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(spyCfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());

        String result = fusionResponse.getResponseAsString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        // System.out.println("RESPONSE " + result);
        return result;
    }

    @Test
    public void testQueryWithMultipleServersMultipleDocumentsJson()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", ResponseRendererType.JSON, "fusion-schema-uni-leipzig.xml",
            Arrays.asList(new SolrFusionRequestParam("authorized_mode:\"false\""),
                new SolrFusionRequestParam("{!tag=format_filter}(format:\"BluRayDisc\")")));
        Multimap<String> expectedParams = buildParams("title:abc", "title", "title", "author");
        expectedParams.put(FILTER_QUERY, "format:\"BluRayDisc\"");
        verify(testAdapter9000, times(1)).sendQuery(expectedParams, 4000);
        // System.out.println("XML " + xml);
    }
}
