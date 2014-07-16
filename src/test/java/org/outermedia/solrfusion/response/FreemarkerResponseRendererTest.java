package org.outermedia.solrfusion.response;

import com.google.gson.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.script.ScriptException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by stephan on 17.06.14.
 */
public class FreemarkerResponseRendererTest
{
    protected TestHelper helper;
    private  Util xmlUtil;
    protected Configuration cfg;

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        xmlUtil = new Util();
    }

    @Test
    public void freemarkerXmlTest() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException {

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-freemarker-response-renderer.xml");

        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(ResponseRendererType.XML);

        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9000.xml", null);
        List<Document> documents9000 = response9000.getDocuments();
        XmlResponse response9001= xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);
        List<Document> documents9001 = response9001.getDocuments();

//        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getResult().getNumFound());
        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9001.getNumFound());
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9000.getResult().getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator =  new MappingClosableIterator(docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);

        FusionRequest req = new FusionRequest();
        req.setQuery("steak");
        String xmlResponse = responseRenderer.getResponseString(cfg, closableIterator, req);
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertFalse("xml response should not contain filter query in header", xmlResponse.contains("<str name=\"fq\">"));

        req.setFilterQuery("salat");
        xmlResponse = responseRenderer.getResponseString(cfg, closableIterator, req);
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertTrue("xml response should contain filter query in header",
            xmlResponse.contains("<str name=\"fq\"><![CDATA[salat]]></str>"));

        System.out.println(xmlResponse);
    }

    @Test
    public void freemarkerJsonTest() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException, ScriptException {

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-freemarker-response-renderer.xml");

        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(ResponseRendererType.JSON);

        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-freemarker.xml", null);
        List<Document> documents9000 = response9000.getDocuments();
//        XmlResponse response9001= xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);
//        List<Document> documents9001 = response9001.getDocuments();

        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getNumFound());
//        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9000.getNumFound());
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9000.getDocuments(), info9000);
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator =  new MappingClosableIterator(docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null);

        FusionRequest req = new FusionRequest();
        req.setQuery("Shakespeares");

        String jsonResponse = responseRenderer.getResponseString(cfg, closableIterator, req);
//        System.out.println(jsonResponse);

        try
        {
            JsonParser parser = new JsonParser();
            Gson gson = new GsonBuilder().create();

            JsonElement el = parser.parse(jsonResponse);
            jsonResponse = gson.toJson(el); // done
//            System.out.println(jsonResponse);

            String expected = "{\"responseHeader\":{\"status\":0,\"QTime\":0,\"params\":{\"indent\":\"on\",\"start\":\"0\",\"q\":\"Shakespeares\",\"wt\":\"json\",\"version\":\"2.2\",\"rows\":\"1\"}},\"response\":{\"numFound\":1,\"start\":0,\"docs\":[{\"singlevalueAsMultivalue\":[\"Shakespeare\"],\"multivalueAsMultivalueWithOneValue\":[\"Poe\"],\"multivalueAsSinglevalueWithOneValue\":[\"Poe\"],\"multivalueAsMultivalueWithTwoValue\":[\"Poe\",\"Morgenstern\"],\"singlevalueAsSingleValue\":\"Shakespeare\"}]}}";
            Assert.assertEquals("Got different json response than expected", expected, jsonResponse);

        }
        catch (JsonSyntaxException jse)
        {
            Assert.fail("Exception while parsing rendered json response");
        }

        Assert.assertFalse("json response should not contain filter query in header", jsonResponse.contains("\"fq\":"));

        req.setFilterQuery("salat");
        jsonResponse = responseRenderer.getResponseString(cfg, closableIterator, req);
        // System.out.println(jsonResponse);
        Assert.assertTrue("json response should contain filter query in header", jsonResponse.contains("\"fq\":\"salat\","));
    }

}
