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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;
import org.outermedia.solrfusion.adapter.solr.Version;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class EmbeddedSolrServerControllerTest extends SolrServerDualTestBase
{
    protected TestHelper helper;

    @Mock ResponseRendererIfc testRenderer;

    ByteArrayInputStream testResponse;

    @Mock SearchServerAdapterIfc testAdapter;

    SearchServerAdapterIfc<SolrFusionUriBuilderIfc> testAdapter9000;

    SearchServerAdapterIfc<SolrFusionUriBuilderIfc> testAdapter9002;

    Configuration cfg;

    @Mock
    private SearchServerConfig testSearchConfig;
    private Configuration spyCfg;
    private FusionRequest fusionRequest;
    private SearchServerConfig searchServerConfig9000;
    private SearchServerConfig searchServerConfig9002;

    public void fillSolr() throws Exception
    {
        Document document = new Document();
        document.addField("id", String.valueOf(1));
        document.addField("title", String.valueOf("abc"));
        document.addField("author", String.valueOf("Shakespeare"));
        testAdapter9000.add(document);
        testAdapter9000.commitLastDocs();

        document = new Document();
        document.addField("id", String.valueOf(1));
        document.addField("titleVT_eng", String.valueOf("abc"));
        document.addField("author", String.valueOf("Shakespeare"));

        testAdapter9002.add(document);
        testAdapter9002.commitLastDocs();
    }

    @After
    public void cleanSolr() throws Exception
    {
        testAdapter9000.deleteByQuery("*:*");
        testAdapter9002.deleteByQuery("*:*");
        testAdapter9000.finish();
        testAdapter9002.finish();
    }

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments() throws Exception
    {
        String xml = testMultipleServers("title:xyz", "title:XYZ");
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"rows\"><![CDATA[0]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:xyz]]></str>\n" +
            "    <str name=\"fl\"><![CDATA[id title score]]></str>\n" +
            "    <arr name=\"fq\">\n" +
            "        <str>title:XYZ</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">xml</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expectedXml, xml.trim());
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildMap(QUERY, "title:xyz", FILTER_QUERY, "title:XYZ", FIELDS_TO_RETURN, "id title score", WRITER_TYPE,
                "xml"), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildMap(QUERY, "titleVT_eng:xyz", FILTER_QUERY, "titleVT_eng:XYZ", FIELDS_TO_RETURN,
                "id titleVT_eng score", WRITER_TYPE, "xml"), new Version("3.6"));
    }

    protected Multimap<String> buildMap(Object... v)
    {
        Multimap<String> result = new Multimap<>();
        for (int i = 0; i < v.length; i += 2)
        {
            result.put(((SolrFusionRequestParams) v[i]), (String) v[i + 1]);
        }
        return result;
    }

    @Test
    public void testQueryWithMultipleServersAndResponseDocuments() throws Exception
    {
        String xml = testMultipleServers("title:abc", "title:abc");
        String sep = DefaultIdGenerator.SEPARATOR;
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "<lst name=\"responseHeader\">\n" +
            "  <int name=\"status\">0</int>\n" +
            "  <int name=\"QTime\">0</int>\n" +
            "  <lst name=\"params\">\n" +
            "    <str name=\"indent\">on</str>\n" +
            "    <str name=\"rows\"><![CDATA[2]]></str>\n" +
            "    <str name=\"q\"><![CDATA[title:abc]]></str>\n" +
            "    <str name=\"fl\"><![CDATA[id title score]]></str>\n" +
            "    <arr name=\"fq\">\n" +
            "        <str>title:abc</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">xml</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"2\" start=\"0\">\n" +
            "    <doc>\n" +
            "        <str name=\"id\">Bibliothek9002" + sep + "1</str>\n" +
            "        <str name=\"title\">abc</str>\n" +
            "        <float name=\"score\">0.6750762040000001</float>\n" +
            "    </doc>\n" +
            "    <doc>\n" +
            "        <str name=\"id\">Bibliothek9000" + sep + "1</str>\n" +
            "        <float name=\"score\">0.36822338400000004</float>\n" +
            "        <str name=\"title\">abc</str>\n" +
            "    </doc>\n" +
            "</result>\n" +
            "</response>";

        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9000, fusionRequest,
            buildMap(QUERY, "title:abc", FILTER_QUERY, "title:abc", FIELDS_TO_RETURN, "id title score", WRITER_TYPE,
                "xml"), new Version("3.6"));
        verify(testAdapter9002, times(1)).buildHttpClientParams(spyCfg, searchServerConfig9002, fusionRequest,
            buildMap(QUERY, "titleVT_eng:abc", FILTER_QUERY, "titleVT_eng:abc", FIELDS_TO_RETURN,
                "id titleVT_eng score", WRITER_TYPE, "xml"), new Version("3.6"));
    }

    protected String testMultipleServers(String queryStr, String filterQueryStr) throws Exception
    {
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-embedder-solr-adapter.xml");
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

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);

        fillSolr();

        FusionControllerIfc fc = cfg.getController();
        fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam(filterQueryStr)));
        fusionRequest.setResponseType(ResponseRendererType.XML);
        fusionRequest.setFieldsToReturn(new SolrFusionRequestParam("id title " + fusionRequest.getFusionSortField()));
        FusionResponse fusionResponse = new FusionResponse();
        StringWriter sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        FusionResponse fusionResponseSpy = spy(fusionResponse);
        doReturn(0l).when(fusionResponseSpy).getQueryTime();
        fc.process(spyCfg, fusionRequest, fusionResponseSpy);
        Assert.assertTrue("Expected no processing error: " + fusionResponseSpy.getErrorMessage(), fusionResponseSpy.isOk());

        String result = sw.toString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        System.out.println("RESPONSE " + result);
        return result;
    }

}
