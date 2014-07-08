package org.outermedia.solrfusion.response;

import com.google.gson.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
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

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-response-renderer.xml");

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
        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9000.getNumFound());
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9000.getResult().getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator =  new MappingClosableIterator(docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0));

        String xmlResponse = responseRenderer.getResponseString(closableIterator, "steak", null);
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertFalse("xml response should not contain filter query in header", xmlResponse.contains("<str name=\"fq\">"));

        xmlResponse = responseRenderer.getResponseString(closableIterator, "steak", "salat");
        Assert.assertNotNull("xmlResponse is expected to be not null", xmlResponse);
        Assert.assertTrue("xml response should contain filter query in header",
            xmlResponse.contains("<str name=\"fq\"><![CDATA[salat]]></str>"));

        System.out.println(xmlResponse);
    }

    @Test
    public void freemarkerJsonTest() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException, ScriptException {

        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-response-renderer.xml");

        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        ResponseRendererIfc responseRenderer = spyCfg.getSearchServerConfigs().getResponseRendererByType(ResponseRendererType.JSON);

        Assert.assertNotNull("responseRenderer should not be null", responseRenderer);

        XmlResponse response9000 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9000.xml", null);
        List<Document> documents9000 = response9000.getDocuments();
        XmlResponse response9001= xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);
        List<Document> documents9001 = response9001.getDocuments();

//        SearchServerResponseInfo info9000 = new SearchServerResponseInfo(response9000.getResult().getNumFound());
        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9000.getNumFound());
//        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9000.getResult().getDocuments(), info9000);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(response9001.getDocuments(), info9001);

//        consolidator.addResultStream(configuration, searchServerConfig, docIterator);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator =  new MappingClosableIterator(docIterator, spyCfg, spyCfg.getConfigurationOfSearchServers().get(0));

        String jsonResponse = responseRenderer.getResponseString(closableIterator, "steak", null);

        try
        {
            JsonParser parser = new JsonParser();
            Gson gson = new GsonBuilder().create();

            JsonElement el = parser.parse(jsonResponse);
            jsonResponse = gson.toJson(el); // done
            System.out.println(jsonResponse);
        }
        catch (JsonSyntaxException jse)
        {
            Assert.fail("Exception while parsing rendered json response");
        }

        Assert.assertFalse("json response should not contain filter query in header", jsonResponse.contains("\"fq\":"));

        jsonResponse = responseRenderer.getResponseString(closableIterator, "steak", "salat");
        System.out.println(jsonResponse);
        Assert.assertFalse("json response should contain filter query in header", jsonResponse.contains("\"fq\":\"salat\","));
    }

}
