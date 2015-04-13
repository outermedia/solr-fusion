package org.outermedia.solrfusion.response.javabin;

import lombok.extern.slf4j.Slf4j;
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
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.HighlightingMap;
import org.outermedia.solrfusion.response.MappingClosableIterator;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 3/25/15.
 */

@Slf4j
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
        Configuration spyCfg = init("test-fusion-schema-9000-9002.xml");

        XmlResponse response9001 = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);

        ByteArrayOutputStream bos = buildJavabinData(spyCfg, response9001, null);

        // now read the byte-array back
        String response = parseJavabinWithSolr(bos).toString();
        // System.out.println(response);
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

    protected SimpleOrderedMap<Object> parseJavabinWithSolr(ByteArrayOutputStream bos) throws IOException
    {
        JavaBinCodec solrCodec = new JavaBinCodec();
        SimpleOrderedMap<Object> namedListResponse = (SimpleOrderedMap<Object>) solrCodec.unmarshal(
            new ByteArrayInputStream(bos.toByteArray()));
        return namedListResponse;
    }

    protected ByteArrayOutputStream buildJavabinData(Configuration spyCfg, XmlResponse response9001,
        Map<String, Document> allHighlighting) throws InvocationTargetException, IllegalAccessException
    {
        SearchServerResponseInfo info9001 = new SearchServerResponseInfo(response9001.getNumFound(), allHighlighting,
            null, null);
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
        return bos;
    }

    protected Configuration init(String xmlPath)
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException,
        InvocationTargetException, IllegalAccessException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation(xmlPath);
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();
        xmlUtil = new Util();
        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);
        return spyCfg;
    }

    @Test
    public void testHighlighting()
        throws IllegalAccessException, ParserConfigurationException, JAXBException, IOException, SAXException,
        InvocationTargetException
    {
        Configuration spyCfg = init("fusion-schema-test1.xml");
        XmlResponse responseFacetsHighlights = xmlUtil.unmarshal(XmlResponse.class,
            "solr-example-response-with-highlights.xml", null);
        Map<String, Document> allHighlighting = new HashMap<>();
        List<Highlighting> solrHl = responseFacetsHighlights.getHighlighting();
        List<Document> solrInputHl = new ArrayList<>();
        SearchServerConfig searchServerConfig = spyCfg.getConfigurationOfSearchServers().get(0);
        for (Highlighting hl : solrHl)
        {
            Document doc = hl.getDocument(searchServerConfig.getIdFieldName());
            solrInputHl.add(doc);
        }

        ClosableListIterator<Document, SearchServerResponseInfo> highlightingIt = new ClosableListIterator<>(
            solrInputHl, null);
        MappingClosableIterator mapper = getNewMappingClosableIterator(spyCfg, searchServerConfig, highlightingIt, null,
            ResponseTarget.HIGHLIGHT);
        HighlightingMap hm = new HighlightingMap(false, spyCfg.allSearchServerNames());
        hm.init(spyCfg.getIdGenerator());
        while (mapper.hasNext())
        {
            Document doc = mapper.next();
            hm.put(doc);
        }
        Map<String, Document> map = hm.getMap();
        ByteArrayOutputStream bos = buildJavabinData(spyCfg, responseFacetsHighlights, map);

        SimpleOrderedMap<Object> response = parseJavabinWithSolr(bos);
        // highlighting={ ,,, }
        SimpleOrderedMap parsedHighlighting = (SimpleOrderedMap) response.get("highlighting");
        // System.out.println("PHL: " + parsedHighlighting);
        Assert.assertEquals("Mapped highlight number is different to original", map.keySet().size(),
            parsedHighlighting.size());
        String expectedDocs[] = {
            "TEST1_VDBDB1A16={name=[A-DATA V-Series", "TEST1_TWINX2048-3200PRO={name=[CORSAIR XMS",
            "TEST1_VS1GB400C3={name=[CORSAIR ValueSelect"
        };
        String hlStr = parsedHighlighting.toString();
        for (String expectedDoc : expectedDocs)
        {
            Assert.assertTrue("Parsed highlights:\n" + hlStr + " don't contain:\n" + expectedDoc,
                hlStr.contains(expectedDoc));
        }
    }

    protected MappingClosableIterator getNewMappingClosableIterator(Configuration config,
        SearchServerConfig searchServerConfig, ClosableIterator<Document, SearchServerResponseInfo> docIterator,
        Set<String> searchServerFieldsToMap, ResponseTarget target)
        throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, config, searchServerConfig, searchServerFieldsToMap, target,
            false);
    }
}
