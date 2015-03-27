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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.outermedia.solrfusion.adapter.solr.SolrFusionUriBuilder;
import org.outermedia.solrfusion.adapter.solr.Version;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow", null)));
        fusionRequest.setFacet(new SolrFusionRequestParam("true", null));
        fusionRequest.setFacetPrefix(new SolrFusionRequestParam("p1", null));
        fusionRequest.setFacetMincount(new SolrFusionRequestParam("2", null));
        fusionRequest.setFacetLimit(new SolrFusionRequestParam("20", null));
        fusionRequest.setFacetSort(new SolrFusionRequestParam("index", null));
        List<SolrFusionRequestParam> facetFields = new ArrayList<>();
        facetFields.add(new SolrFusionRequestParam("{!ex=format_filter}format", null));
        facetFields.add(new SolrFusionRequestParam("access_facet", null));
        fusionRequest.setFacetFields(facetFields);
        List<SolrFusionRequestParam> facetSortFields = new ArrayList<>();
        facetSortFields.add(new SolrFusionRequestParam("index1", "finc_class_facet", null));
        facetSortFields.add(new SolrFusionRequestParam("index2", "format", null));
        fusionRequest.setFacetSortFields(facetSortFields);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        // System.out.println("ERROR " + fusionResponse.getErrorMessage());
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());
    }

    protected Multimap<String> buildParams(String q, String title2, String title1, String author, String responseFormat)
    {
        Multimap<String> result = super.buildParams(q, null);
        result.set(FIELDS_TO_RETURN, "* score id");
        result.put(FACET, "true");
        result.put(FACET_PREFIX, "p1");
        result.put(FACET_MINCOUNT, "2");
        result.put(FACET_LIMIT, "20");
        result.set(WRITER_TYPE, responseFormat);
        result.put(FACET_SORT, "index");
        if (title1 != null)
        {
            result.put(FACET_FIELD, "{!ex=format_filter}" + title1);
        }
        result.put(FACET_FIELD, "{!ex=format_filter}" + title2);
        if (author != null)
        {
            result.put(FACET_FIELD, author);
        }
        if (title1 != null)
        {
            String facetSortField2 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(title1, Locale.GERMAN);
            result.put(facetSortField2, "index1");
        }
        String facetSortField2 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(title2, Locale.GERMAN);
        result.put(facetSortField2, "index1");
        if (author != null)
        {
            String facetSortField3 = FACET_SORT_FIELD.buildFusionFacetSortFieldParam(author, Locale.GERMAN);
            result.put(facetSortField3, "index2");
        }
        return result;
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsXml()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.XML, null);
        String xml = testMultipleServers("target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", "test-fusion-schema-9000-9002.xml", fusionRequest, 0, 1);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
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
            "    <str name=\"wt\">xml</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildParams("title:abc", "title", null, "author9000", "xml"), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildParams("titleVT_eng:abc", "titleVT_eng", null, "author9002", "xml"), new Version("3.6"));
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocumentsJson()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.JSON, null);
        String xml = testMultipleServers("target/test-classes/test-empty-xml-response.xml",
            "target/test-classes/test-empty-xml-response.xml", "test-fusion-schema-9000-9002.xml", fusionRequest, 0, 1);
        String expected = "{\n" +
            "  \"responseHeader\":{\n" +
            "    \"status\":0,\n" +
            "    \"QTime\":0,\n" +
            "    \"params\":{\n" +
            "      \"indent\":\"on\",\n" +
            "      \"rows\":\"0\",\n" +
            "      \"q\":\"title:abc\",\n" +
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
            "      \"version\":\"2.2\"}}\n" +
            "  , \"response\":{\"numFound\":0,\"start\":0,\"docs\":[\n" +
            "  ]}\n" +
            "}";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildParams("title:abc", "title", null, "author9000", "xml"), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildParams("titleVT_eng:abc", "titleVT_eng", null, "author9002", "xml"), new Version("3.6"));
    }

    protected String testMultipleServers(String responseServer1, String responseServer2, String fusionSchemaPath,
        FusionRequest fusionRequest, int firstServer, int secondServer)
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
        spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        List<SearchServerConfig> searchServerConfigs = spyCfg.getSearchServerConfigs().getSearchServerConfigs();
        searchServerConfig9000 = spy(searchServerConfigs.get(firstServer));
        searchServerConfig9002 = spy(searchServerConfigs.get(secondServer));
        searchServerConfigs.clear();

        searchServerConfigs.add(searchServerConfig9000);
        testAdapter9000 = spy(searchServerConfig9000.getInstance());
        when(searchServerConfig9000.getInstance()).thenReturn(testAdapter9000);
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(any(SolrFusionUriBuilder.class), Mockito.anyInt());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(any(SolrFusionUriBuilder.class), Mockito.anyInt());

        FusionControllerIfc fc = cfg.getController();
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

    private FusionRequest getFusionRequest(String queryStr, ResponseRendererType format,
        List<SolrFusionRequestParam> filterQueries)
    {
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setResponseType(format);
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr, null));
        fusionRequest.setFacet(new SolrFusionRequestParam("true", null));
        fusionRequest.setFacetPrefix(new SolrFusionRequestParam("p1", null));
        fusionRequest.setFacetMincount(new SolrFusionRequestParam("2", null));
        fusionRequest.setFacetLimit(new SolrFusionRequestParam("20", null));
        fusionRequest.setFacetSort(new SolrFusionRequestParam("index", null));
        List<SolrFusionRequestParam> facetFields = new ArrayList<>();
        facetFields.add(new SolrFusionRequestParam("{!ex=format_filter}title", null));
        facetFields.add(new SolrFusionRequestParam("author", null));
        fusionRequest.setFacetFields(facetFields);
        List<SolrFusionRequestParam> facetSortFields = new ArrayList<>();
        facetSortFields.add(new SolrFusionRequestParam("index1", "title", null));
        facetSortFields.add(new SolrFusionRequestParam("index2", "author", null));
        fusionRequest.setFacetSortFields(facetSortFields);
        fusionRequest.setFilterQuery(filterQueries);
        return fusionRequest;
    }

    @Test
    public void testQueryWithMultipleServersMultipleDocumentsJson()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        final List<SolrFusionRequestParam> filterQueries = Arrays.asList(
            new SolrFusionRequestParam("authorized_mode:\"false\"", null),
            new SolrFusionRequestParam("{!tag=format_filter}(format:\"BluRayDisc\")", null));
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.JSON, filterQueries);
        String json = testMultipleServers("target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", "fusion-schema-uni-leipzig.xml", fusionRequest, 0, 1);
        Multimap<String> expectedParams = buildParams("title:abc", "title", null, "author", "xml");
        expectedParams.set(FILTER_QUERY, "{!tag=format_filter}format:\"BluRayDisc\"");
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest, expectedParams,
            new Version("3.5"));
        // System.out.println("XML " + xml);
    }

    @Test
    public void testAddInFacetQueryParams()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.XML, null);
        fusionRequest.getFacetFields().remove(1);
        String xml = testMultipleServers("target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", "fusion-schema-uni-leipzig.xml", fusionRequest, 0, 2);
        // System.out.println("XML " + xml);
        Multimap<String> expectedParams = buildParams("title:abc", "titleVT_eng", "titleVT_de", null, "xml");
        expectedParams.set("q", "(titleVT_de:abc OR titleVT_eng:abc)");
        expectedParams.delete("fq");
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest, expectedParams,
            new Version("3.6"));
    }

    @Test
    public void testAddInFacetResponse()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.XML, null);
        fusionRequest.getFacetSortFields().add(new SolrFusionRequestParam("count", "author_facet", null));
        fusionRequest.getFacetFields().remove(1);
        String xml = testMultipleServers("target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", "fusion-schema-uni-leipzig.xml", fusionRequest, 0, 1);
        // System.out.println("XML " + xml);
        String expectedFacets = "<lst name=\"author_facet\">\n" +
            "            <int name=\"Schiller, Friedrich 1759 - 1805\">2572</int>\n" +
            "            <int name=\"Beethoven, Ludwig van Komponist 1770-1827\">255</int>\n" +
            "            <int name=\"Goethe, Johann Wolfgang von 1749 - 1832\">192</int>\n" +
            "            <int name=\"Schiller-Nationalmuseum und Deutsches Literaturarchiv\">181</int>\n" +
            "            <int name=\"Universität Jena\">176</int>\n" +
            "            <int name=\"Beethoven, Ludwig van 1770-1827\">171</int>\n" +
            "            <int name=\"Schubert, Franz\">140</int>\n" +
            "            <int name=\"Schiller, Friedrich\">135</int>\n" +
            "            <int name=\"1759\">131</int>\n" +
            "            <int name=\"1805\">131</int>\n" +
            "            <int name=\"Schubert, Franz Komponist 1797-1828\">117</int>\n" +
            "            <int name=\"Schubert, Franz 1797-1828\">80</int>\n" +
            "            <int name=\"Mozart, Wolfgang Amadeus Komponist 1756-1791\">76</int>\n" +
            "            <int name=\"C. Schiller\">75</int>\n" +
            "            <int name=\"Schubert, Franz 1797 - 1828\">74</int>\n" +
            "            <int name=\"Beethoven, Ludwig van 1770 - 1827\">66</int>\n" +
            "            <int name=\"Philharmonia Orchestra\">64</int>\n" +
            "            <int name=\"Bach, Johann Sebastian Komponist 1685-1750\">59</int>\n" +
            "            <int name=\"Brahms, Johannes Komponist 1833-1897\">59</int>\n" +
            "            <int name=\"Berliner Philharmoniker\">58</int>\n" +
            "        </lst>";
        Assert.assertTrue("Found other facets than expected", xml.contains(expectedFacets));
    }

    @Test
    public void testDropInFacetFieldParams()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionRequest fusionRequest = getFusionRequest("title:abc", ResponseRendererType.XML, null);
        fusionRequest.getFacetFields().remove(1);
        fusionRequest.getFacetFields().add(new SolrFusionRequestParam("solr-server"));
        String xml = testMultipleServers("target/test-classes/test-schiller-9000.xml",
            "target/test-classes/test-schiller-9001.xml", "fusion-schema-uni-leipzig.xml", fusionRequest, 0, 2);
        // System.out.println(xml);
        Multimap<String> expectedParams = buildParams("title:abc", "titleVT_eng", "titleVT_de", null, "xml");
        expectedParams.set("q", "(titleVT_de:abc OR titleVT_eng:abc)");
        expectedParams.delete("fq");
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest, expectedParams,
            new Version("3.6"));
        System.out.println(expectedParams);
    }
}
