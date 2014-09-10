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
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ControllerFilterQueryTest extends AbstractControllerTest
{

    private Configuration spyCfg;
    private FusionRequest fusionRequest;
    private SearchServerConfig searchServerConfig9000;
    private SearchServerConfig searchServerConfig9002;

    @Test
    public void testProcessWithoutFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        checkFilterQueryHandling(null);
    }

    @Test
    public void testProcessWithOneFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        // with one fq
        checkFilterQueryHandling(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow")));
    }

    @Test
    public void testProcessWithTwoFilterQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        checkFilterQueryHandling(Arrays.asList(new SolrFusionRequestParam("author:Goethe -title:tomorrow"),
            new SolrFusionRequestParam("author:Wolfgang")));
    }

    protected void checkFilterQueryHandling(List<SolrFusionRequestParam> filterQueries)
        throws IllegalAccessException, ParserConfigurationException, IOException, JAXBException, URISyntaxException,
        SAXException, InvocationTargetException
    {
        FusionControllerIfc fc = createTestFusionController("test-query-mapper-fusion-schema.xml");
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen"));
        fusionRequest.setFilterQuery(filterQueries);
        FusionResponse fusionResponse = new FusionResponse();
        fc.process(cfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error: " + fusionResponse.getErrorMessage(), fusionResponse.isOk());
    }

    @Test
    public void testQueryParsingFailed()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        FusionControllerIfc fc = createTestFusionController("test-empty-fusion-schema.xml");
        cfg.getSearchServerConfigs().setDisasterLimit(3); // only one server configured
        FusionRequest fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam("author:Schiller -title:morgen"));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam("author:*:Schiller")));

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
        testMultipleServers("title:abc", "title:def", "target/test-classes/test-xml-response-9000.xml",
            "target/test-classes/test-xml-response-9002.xml");
        verify(testAdapter9000, times(1)).sendQuery(spyCfg, searchServerConfig9000, fusionRequest, buildParams("title:abc", "title:def"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(spyCfg, searchServerConfig9002, fusionRequest, buildParams("titleVT_eng:abc", "titleVT_eng:def"), 4000,
            "3.6");
    }

    @Test
    public void testQueryWithMultipleServersButNoResponseDocuments()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        String xml = testMultipleServers("title:abc", "title:def", "target/test-classes/test-empty-xml-response.xml",
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
            "    <arr name=\"fq\">\n" +
            "        <str>title:def</str>\n" +
            "    </arr>\n" +
            "    <str name=\"wt\">wt</str>\n" +
            "    <str name=\"version\">2.2</str>\n" +
            "  </lst>\n" +
            "</lst>\n" +
            "<result name=\"response\" numFound=\"0\" start=\"0\">\n" +
            "</result>\n" +
            "</response>";
        Assert.assertEquals("Found different xml response", expected, xml.trim());
        verify(testAdapter9000, times(1)).sendQuery(spyCfg, searchServerConfig9000, fusionRequest, buildParams("title:abc", "title:def"), 4000, "3.6");
        verify(testAdapter9002, times(1)).sendQuery(spyCfg, searchServerConfig9002, fusionRequest, buildParams("titleVT_eng:abc", "titleVT_eng:def"), 4000,
            "3.6");
    }

    protected String testMultipleServers(String queryStr, String filterQueryStr, String responseServer1,
        String responseServer2)
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
        doReturn(documents9000Stream).when(testAdapter9000).sendQuery(any(Configuration.class), any(SearchServerConfig.class),
            any(FusionRequest.class), any(Multimap.class), Mockito.anyInt(),
            anyString());

        searchServerConfigs.add(searchServerConfig9002);
        testAdapter9002 = spy(searchServerConfig9002.getInstance());
        when(searchServerConfig9002.getInstance()).thenReturn(testAdapter9002);
        doReturn(documents9002Stream).when(testAdapter9002).sendQuery(any(Configuration.class), any(SearchServerConfig.class),
            any(FusionRequest.class), any(Multimap.class), Mockito.anyInt(),
            anyString());

        FusionControllerIfc fc = cfg.getController();
        fusionRequest = new FusionRequest();
        fusionRequest.setQuery(new SolrFusionRequestParam(queryStr));
        fusionRequest.setFilterQuery(Arrays.asList(new SolrFusionRequestParam(filterQueryStr)));
        fusionRequest.setResponseType(ResponseRendererType.XML);
        FusionResponse fusionResponse = spy(new FusionResponse());
        doReturn(0l).when(fusionResponse).getQueryTime();
        fc.process(spyCfg, fusionRequest, fusionResponse);
        Assert.assertTrue("Expected no processing error", fusionResponse.isOk());

        String result = fusionResponse.getResponseAsString();
        Assert.assertNotNull("Expected XML result, but got nothing", result);
        // System.out.println("RESPONSE " + result);
        return result;
    }
}
