package org.outermedia.solrfusion.response.javabin;

import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.MappingClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 3/25/15.
 */

public class JavaBinTest
{
    protected TestHelper helper;
    protected Configuration cfg;
    protected Util xmlUtil;

    @Test
    public void testCreate()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        xmlUtil = new Util();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        XmlResponse response9001 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);

        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9001.getNumFound(), null, null, null);
        ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
            response9001.getDocuments(), info9001);

        ClosableIterator<Document, SearchServerResponseInfo> closableIterator = new MappingClosableIterator(docIterator,
            spyCfg, spyCfg.getConfigurationOfSearchServers().get(0), null, ResponseTarget.ALL, true);

        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("steak"));
        req.setSort(new SolrFusionRequestParam("title asc"));
        req.setStart(new SolrFusionRequestParam("7"));
        FusionResponse fusionResponse = new FusionResponse();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fusionResponse.setBinWriter(new BufferedOutputStream(bos));
        fusionResponse.setOk();

        JavaBin4 javaBinRenderer = new JavaBin4();
        javaBinRenderer.writeResponse(cfg, closableIterator, req, fusionResponse);

        // now read the byte-array back
        JavaBinCodec solrCodec = new JavaBinCodec();
        SimpleOrderedMap<Object> namedListResponse = (SimpleOrderedMap<Object>) solrCodec.unmarshal(
            new ByteArrayInputStream(bos.toByteArray()));
        String response = namedListResponse.toString();
        System.out.println(response);
        Assert.assertFalse("Didn't expect to find errors, but got: " + response, response.contains("error={"));
        String docs[] = {
            "SolrDocument{id=Bibliothek9000_NLZ042429315,", "SolrDocument{id=Bibliothek9000_340732091,",
            "SolrDocument{id=Bibliothek9000_340733039", "SolrDocument{id=Bibliothek9000_340737689,",
            "SolrDocument{id=Bibliothek9000_340737980,"
        };
        for (String docId : docs)
        {
            Assert.assertTrue("Expected to find doc '" + docId + "', but found: " + response, response.contains(docId));
        }
    }

}
