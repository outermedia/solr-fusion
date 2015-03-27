package org.outermedia.solrfusion.response;

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

import com.google.gson.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.*;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.*;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Created by stephan on 17.06.14.
 */
public class FreemarkerResponseRendererTest
{
    protected TestHelper helper;
    private Util xmlUtil;
    protected Configuration cfg;

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        xmlUtil = new Util();
    }

    @Test
    public void freemarkerXmlTest()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-freemarker-response-renderer.xml");

        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.XML);

        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9000.xml", null);
        List<Document> documents9000 = response9000.getDocuments();
        XmlResponse response9001 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);
        List<Document> documents9001 = response9001.getDocuments();

        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9001.getNumFound(), null, null, null);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9001.getDocuments(), info9001);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator = new MappingClosableIterator(docIterator,
            spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null, ResponseTarget.ALL,true);

        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("steak"));
        req.setSort(new SolrFusionRequestParam("title asc"));
        req.setStart(new SolrFusionRequestParam("7"));
        FusionResponse fusionResponse = new FusionResponse();
        StringWriter sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        responseRenderer.writeResponse(cfg, closableIterator, req, fusionResponse);
        String xmlResponse = sw.toString();
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertFalse("xml response should not contain filter query in header",
            xmlResponse.contains("<str name=\"fq\">"));

        fusionResponse = new FusionResponse();
        sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        req.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("salat"), new SolrFusionRequestParam("tomato")));
        responseRenderer.writeResponse(cfg, closableIterator, req, fusionResponse);
        xmlResponse = sw.toString();
        // System.out.println(xmlResponse);
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertTrue("xml response should contain filter query in header",
            xmlResponse.contains("<arr name=\"fq\">\n" +
                "        <str>salat</str>\n" +
                "        <str>tomato</str>\n" +
                "    </arr>"));
        Assert.assertTrue("Expected to find start in header",
            xmlResponse.contains("<str name=\"start\"><![CDATA[7]]></str>"));
        Assert.assertTrue("Expected to find sort in header",
            xmlResponse.contains("<str name=\"sort\"><![CDATA[title asc]]></str>"));
    }

    @Test
    public void freemarkerJsonTest()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException, ScriptException
    {

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-freemarker-response-renderer.xml");

        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.JSON);

        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-freemarker.xml", null);
        List<Document> documents9000 = response9000.getDocuments();

        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), null, null, null);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9000.getDocuments(), info9000);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator = new MappingClosableIterator(docIterator,
            spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null, ResponseTarget.ALL,true);

        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("Shakespeares"));

        FusionResponse res = new FusionResponse();
        StringWriter sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        res.setOk(true);
        responseRenderer.writeResponse(cfg, closableIterator, req, res);
        String jsonResponse = sw.toString();
        // System.out.println("JSON " + jsonResponse);

        try
        {
            JsonParser parser = new JsonParser();
            Gson gson = new GsonBuilder().create();

            JsonElement el = parser.parse(jsonResponse);
            jsonResponse = gson.toJson(el); // done
//            System.out.println(jsonResponse);

            String expected = "{\"responseHeader\":{\"status\":0,\"QTime\":0,\"params\":{\"indent\":\"on\",\"rows\":\"1\",\"q\":\"Shakespeares\",\"wt\":\"json\",\"version\":\"2.2\"}},\"response\":{\"numFound\":1,\"start\":0,\"docs\":[{\"singlevalueAsMultivalue\":[\"Shakespeare\"],\"multivalueAsMultivalueWithOneValue\":[\"Poe\"],\"multivalueAsMultivalueWithTwoValue\":[\"Poe\",\"Morgenstern\"],\"singlevalueAsSingleValue\":\"Shakespeare\",\"multivalueAsSinglevalueWithOneValue\":\"Poe\"}]}}";
            Assert.assertEquals("Got different json response than expected", expected, jsonResponse);

        }
        catch (JsonSyntaxException jse)
        {
            Assert.fail("Exception while parsing rendered json response: " + jse);
        }

        Assert.assertFalse("json response should not contain filter query in header", jsonResponse.contains("\"fq\":"));

        req.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("salat"), new SolrFusionRequestParam("tomato")));
        res = new FusionResponse();
        sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        res.setOk(true);
        responseRenderer.writeResponse(cfg, closableIterator, req, res);
        jsonResponse = sw.toString();
            // System.out.println(jsonResponse);
        Assert.assertTrue("json response should contain filter query in header", jsonResponse.contains("\"fq\":[\n" +
            "        \"salat\",\"tomato\"\n" +
            "      ],"));

        res.setResponseForException(new Exception("An\nerror\noccurred."));
        sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        responseRenderer.writeResponse(cfg, closableIterator, req, res);
        jsonResponse = sw.toString();
            // System.out.println(jsonResponse);
        Assert.assertTrue("json response should contain error header",
            jsonResponse.contains("\"msg\":\"Internal processing error. Reason: An\\nerror\\noccurred.\","));
    }

    @Test
    public void freemarkerJsonHighlightTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.JSON);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String jsonResponse = createResponseHighlights(spyCfg, responseRenderer);

        //System.out.println(jsonResponse);
        Assert.assertTrue("Didn't find highlighting part in json response",
            jsonResponse.contains("\"highlighting\":{"));
        String expectedEmptyHighlight = "\"UBL_0000745438\": {\n" + "    },";
        Assert.assertTrue("Didn't find empty highlight for doc id UBL_0000745438",
            jsonResponse.contains(expectedEmptyHighlight));
        String expectedMultipleHighlight = "\"UBL_0007822387\": {\n" +
            "    \"series2\":[\n" +
            "                    \"Jahresbericht der {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft\"\n" +
            "            ],\n" +
            "    \"series\":[\n" +
            "                    \"{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft Jahresbericht der {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft\"\n" +
            "            ],\n" +
            "    \"title\":[\n" +
            "                    \"{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch <Göttingen>\"\n" +
            "            ],\n" +
            "    \"title_short\":[\n" +
            "                    \"{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch <Göttingen>\"\n" +
            "            ],\n" +
            "    \"title_full\":[\n" +
            "                    \"{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch <Göttingen> 27\"\n" +
            "            ]\n" +
            "    },";
        Assert.assertTrue("Didn't find multiple highlights of doc id UBL_0007822387",
            jsonResponse.contains(expectedMultipleHighlight));
    }

    @Test
    public void freemarkerXmlHighlightTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.XML);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String xmlResponse = createResponseHighlights(spyCfg, responseRenderer);

        // System.out.println("XML Response: "+xmlResponse);
        Assert.assertTrue("Didn't find highlighting part in json response",
            xmlResponse.contains("<lst name=\"highlighting\">"));
        String expectedEmptyHighlight = "<lst name=\"UBL_0000745438\">\n" + "    </lst>";
        Assert.assertTrue("Didn't find empty highlight for doc id UBL_0012096618",
            xmlResponse.contains(expectedEmptyHighlight));
        String expectedMultipleHighlight = "<lst name=\"UBL_0007822387\">\n" +
            "                <arr name=\"series2\">\n" +
            "        <str>Jahresbericht der {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft</str>\n" +
            "                </arr>\n" +
            "                <arr name=\"series\">\n" +
            "        <str>{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft Jahresbericht der {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft</str>\n" +
            "                </arr>\n" +
            "                <arr name=\"title\">\n" +
            "        <str>{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch &lt;Göttingen&gt;</str>\n" +
            "                </arr>\n" +
            "                <arr name=\"title_short\">\n" +
            "        <str>{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch &lt;Göttingen&gt;</str>\n" +
            "                </arr>\n" +
            "                <arr name=\"title_full\">\n" +
            "        <str>{{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Jahrbuch &lt;Göttingen&gt; 27</str>\n" +
            "                </arr>\n" +
            "    </lst>";
        Assert.assertTrue("Didn't find multiple highlights of doc id UBL_0007822387",
            xmlResponse.contains(expectedMultipleHighlight));

        Assert.assertFalse("Expected not to find start in header",
            xmlResponse.contains("<str name=\"start\">"));
        Assert.assertFalse("Expected to find sort in header",
            xmlResponse.contains("<str name=\"sort\">"));
    }

    // fill fusionHighlights from mapped searchServerHighlights (avoids usage of controller)
    protected String createResponseHighlights(Configuration spyCfg, ResponseRendererIfc responseRenderer)
        throws InvocationTargetException, IllegalAccessException, SAXException, ParserConfigurationException,
        FileNotFoundException, JAXBException
    {
        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "response-with-facets-highlighting.xml", null);
        List<Highlighting> searchServerHighlights = response9000.getHighlighting();

        Map<String, Document> fusionHighlights = new HashMap<>();
        List<Document> searchServerHighlightDocs = new ArrayList<>();
        for (Highlighting hl : searchServerHighlights)
        {
            Document document = hl.getDocument("id");
            searchServerHighlightDocs.add(document);
        }
        ClosableListIterator<Document, SearchServerResponseInfo> documentObjectClosableListIterator = new ClosableListIterator<>(
            searchServerHighlightDocs, null);
        ClosableIterator<Document, SearchServerResponseInfo> closableHlIterator = new MappingClosableIterator(
            documentObjectClosableListIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null, ResponseTarget.ALL,true);
        while (closableHlIterator.hasNext())
        {
            Document doc = closableHlIterator.next();
            // System.out.println("HLDOC "+doc.buildFusionDocStr());
            fusionHighlights.put(doc.getFusionDocId("id"), doc);
        }
        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), fusionHighlights,
            null, null);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9000.getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> closableDocIterator = new MappingClosableIterator(
            docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null, ResponseTarget.ALL,true);
        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("goethe"));
        FusionResponse res = new FusionResponse();
        StringWriter sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        res.setOk(true);
        responseRenderer.writeResponse(cfg, closableDocIterator, req, res);
        return sw.toString();
    }

    @Test
    public void freemarkerJsonFacetTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.JSON);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String jsonResponse = createResponseFacets(spyCfg, responseRenderer, "response-with-facets-highlighting.xml");
        // System.out.println(jsonResponse);

        String expectedFacet = "\"facet_fields\":{\n" +
            "      \"solr-server\": [\n" +
            "        [\"UBL\", 20]\n" +
            "      ],\n" +
            "      \"branch_de15\": [";
        Assert.assertTrue("Expected to find facet \n"+expectedFacet+"\n, but got: " + jsonResponse,
            jsonResponse.contains(expectedFacet));
        Assert.assertTrue("Expected to find facet collcode_de15 with [\"Freihand\", 1952], but got: " + jsonResponse,
            jsonResponse.contains("[\"Freihand\", 1952]"));
        Assert.assertTrue("Expected to find facet format_de15 with [\"Software\", 66], but got: " + jsonResponse,
            jsonResponse.contains("[\"Software\", 66]"));
    }

    @Test
    public void freemarkerXmlFacetTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.XML);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String xmlResponse = createResponseFacets(spyCfg, responseRenderer, "response-with-facets-highlighting.xml");
        // System.out.println(xmlResponse);

        String expectedFacet = "<lst name=\"facet_fields\">\n" +
            "        <lst name=\"solr-server\">\n" +
            "            <int name=\"UBL\">20</int>\n" +
            "        </lst>\n" +
            "        <lst name=\"branch_de15\">";
        Assert.assertTrue("Expected to find facet \n"+expectedFacet+"\n, but got: " + xmlResponse,
            xmlResponse.contains(expectedFacet));
        Assert.assertTrue(
            "Expected to find facet collcode_de15 with <int name=\"Freihand\">1952</int>, but got: : " + xmlResponse,
            xmlResponse.contains("<int name=\"Freihand\">1952</int>"));
        Assert.assertTrue(
            "Expected to find facet format_de15 with <int name=\"Software\">66</int>, but got: : " + xmlResponse,
            xmlResponse.contains("<int name=\"Software\">66</int>"));
    }

    protected String createResponseFacets(Configuration spyCfg, ResponseRendererIfc responseRenderer,
        String responseData) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, responseData, null);
        Document facets = response9000.getFacetFields("id", 1);
        SearchServerConfig searchServerConfig = spyCfg.getConfigurationOfSearchServers().get(0);
        PagingResponseConsolidator consolidator = (PagingResponseConsolidator) spyCfg.getResponseConsolidatorFactory().getInstance();
        consolidator.initConsolidator(cfg);
        consolidator.rememberTotalDocsFound(searchServerConfig.getSearchServerName(), 20);
        consolidator.processFacetFields(cfg, searchServerConfig, facets);
        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("goethe"));
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        Map<String, List<DocCount>> sortedFacets = consolidator.mapFacetDocCounts(cfg.getIdGenerator(),
            cfg.getFusionIdFieldName(), req);
        // System.out.println("FACETS "+sortedFacets);
        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), null, sortedFacets,
            null);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            new ArrayList<Document>(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> closableDocIterator = new MappingClosableIterator(
            docIterator, spyCfg, searchServerConfig, null, ResponseTarget.ALL, true);
        FusionResponse res = new FusionResponse();
        StringWriter sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        res.setOk(true);
        responseRenderer.writeResponse(cfg, closableDocIterator, req, res);
        return sw.toString();
    }

    @Test
    public void freemarkerXmlMoreLikeThisTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.XML);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String xmlResponse = createResponseMoreLikeThis(spyCfg, responseRenderer, "test-more-like-this-response.xml");
        // System.out.println(xmlResponse);
        Assert.assertTrue("Exptected to find match result:\n" + xmlResponse,
            xmlResponse.contains("<result name=\"match\" numFound=\"1\" start=\"0\">"));
        Assert.assertTrue("Exptected to find doc with id UBL_0002688343 in match result:\n" + xmlResponse,
            xmlResponse.contains("<str name=\"id\">UBL_0002688343</str>"));
    }

    @Test
    public void freemarkerJsonMoreLikeThisTest()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(
            ResponseRendererType.JSON);
        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        String xmlResponse = createResponseMoreLikeThis(spyCfg, responseRenderer, "test-more-like-this-response.xml");
        // System.out.println(xmlResponse);
        Assert.assertTrue("Exptected to find match result:\n" + xmlResponse,
            xmlResponse.contains("\"match\":{\"numFound\":1,\"start\":0,\"docs\":["));
        Assert.assertTrue("Exptected to find doc with id UBL_0002688343 in match result:\n" + xmlResponse,
            xmlResponse.contains("\"id\":\"UBL_0002688343\","));
    }

    protected String createResponseMoreLikeThis(Configuration spyCfg, ResponseRendererIfc responseRenderer,
        String responseData) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, responseData, null);
        List<Document> moreLikeThisDocs = response9000.getMatchDocuments();
        SearchServerConfig searchServerConfig = spyCfg.getConfigurationOfSearchServers().get(0);
        PagingResponseConsolidator consolidator = (PagingResponseConsolidator) spyCfg.getResponseConsolidatorFactory().getInstance();
        consolidator.initConsolidator(cfg);

        // map matched docs
        List<Document> mappedMoreLikeThisDocs = new ArrayList<>();
        SearchServerResponseInfo matchInfo9000 = new SearchServerResponseInfo(response9000.getMatchNumFound(), null,
            null, null);
        ClosableIterator<Document, SearchServerResponseInfo> matchDocIterator = new ClosableListIterator<>(
            moreLikeThisDocs, matchInfo9000);
        ClosableIterator<Document, SearchServerResponseInfo> closableMatchDocIterator = new MappingClosableIterator(
            matchDocIterator, spyCfg, searchServerConfig, null, ResponseTarget.ALL, true);
        while (closableMatchDocIterator.hasNext())
        {
            mappedMoreLikeThisDocs.add(closableMatchDocIterator.next());
        }

        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("goethe"));
        req.setFacetLimit(new SolrFusionRequestParam("20"));
        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), null, null,
            mappedMoreLikeThisDocs);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            new ArrayList<Document>(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> closableDocIterator = new MappingClosableIterator(
            docIterator, spyCfg, searchServerConfig, null, ResponseTarget.ALL, true);
        FusionResponse res = new FusionResponse();
        StringWriter sw = new StringWriter();
        res.setTextWriter(new PrintWriter(sw));
        res.setOk(true);
        responseRenderer.writeResponse(cfg, closableDocIterator, req, res);
        return sw.toString();
    }
}