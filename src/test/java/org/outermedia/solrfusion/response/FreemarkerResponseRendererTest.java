package org.outermedia.solrfusion.response;

import com.google.gson.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getResult().getNumFound());
        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9001.getNumFound(), null);
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9000.getResult().getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator = new MappingClosableIterator(docIterator,
            spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);

        FusionRequest req = new FusionRequest();
        req.setQuery("steak");
        String xmlResponse = responseRenderer.getResponseString(cfg, closableIterator, req, new FusionResponse());
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertFalse("xml response should not contain filter query in header",
            xmlResponse.contains("<str name=\"fq\">"));

        req.setFilterQuery("salat");
        xmlResponse = responseRenderer.getResponseString(cfg, closableIterator, req, new FusionResponse());
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertTrue("xml response should contain filter query in header",
            xmlResponse.contains("<str name=\"fq\"><![CDATA[salat]]></str>"));

        System.out.println(xmlResponse);
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
//        XmlResponse response9001= xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);
//        List<Document> documents9001 = response9001.getDocuments();

        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), null);
//        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9000.getNumFound());
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9000.getDocuments(), info9000);
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator = new MappingClosableIterator(docIterator,
            spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);

        FusionRequest req = new FusionRequest();
        req.setQuery("Shakespeares");

        FusionResponse res = new FusionResponse();
        res.setOk(true);
        String jsonResponse = responseRenderer.getResponseString(cfg, closableIterator, req, res);
//        System.out.println(jsonResponse);

        try
        {
            JsonParser parser = new JsonParser();
            Gson gson = new GsonBuilder().create();

            JsonElement el = parser.parse(jsonResponse);
            jsonResponse = gson.toJson(el); // done
//            System.out.println(jsonResponse);

            String expected = "{\"responseHeader\":{\"status\":0,\"QTime\":0,\"params\":{\"indent\":\"on\",\"start\":\"0\",\"q\":\"Shakespeares\",\"wt\":\"json\",\"version\":\"2.2\",\"rows\":\"1\"}},\"response\":{\"numFound\":1,\"start\":0,\"docs\":[{\"singlevalueAsMultivalue\":[\"Shakespeare\"],\"multivalueAsMultivalueWithOneValue\":[\"Poe\"],\"multivalueAsMultivalueWithTwoValue\":[\"Poe\",\"Morgenstern\"],\"singlevalueAsSingleValue\":\"Shakespeare\",\"multivalueAsSinglevalueWithOneValue\":\"Poe\"}]}}";
            Assert.assertEquals("Got different json response than expected", expected, jsonResponse);

        }
        catch (JsonSyntaxException jse)
        {
            Assert.fail("Exception while parsing rendered json response");
        }

        Assert.assertFalse("json response should not contain filter query in header", jsonResponse.contains("\"fq\":"));

        req.setFilterQuery("salat");
        res = new FusionResponse();
        res.setOk(true);
        jsonResponse = responseRenderer.getResponseString(cfg, closableIterator, req, res);
        // System.out.println(jsonResponse);
        Assert.assertTrue("json response should contain filter query in header",
            jsonResponse.contains("\"fq\":\"salat\","));

        res.setResponseForException(new Exception("An\nerror\noccurred."));
        jsonResponse = responseRenderer.getResponseString(cfg, closableIterator, req, res);
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
            documentObjectClosableListIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);
        while (closableHlIterator.hasNext())
        {
            Document doc = closableHlIterator.next();
            // System.out.println("HLDOC "+doc.buildFusionDocStr());
            fusionHighlights.put(doc.getFusionDocId("id"), doc);
        }
        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound(), fusionHighlights);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9000.getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> closableDocIterator = new MappingClosableIterator(
            docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);
        FusionRequest req = new FusionRequest();
        req.setQuery("goethe");
        FusionResponse res = new FusionResponse();
        res.setOk(true);
        return responseRenderer.getResponseString(cfg, closableDocIterator, req, res);
    }
}
