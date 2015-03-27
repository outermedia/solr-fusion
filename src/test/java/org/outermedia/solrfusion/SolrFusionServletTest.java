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

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.GlobalSearchServerConfig;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.xml.sax.SAXException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

@Slf4j
public class SolrFusionServletTest
{

    private SolrFusionServlet servlet;

    static class TestSolrFusionServlet extends SolrFusionServlet
    {
        protected long currentTime;

        @Override protected long getCurrentTimeInMillis()
        {
            return currentTime;
        }
    }

    @Mock ServletConfig servletConfig;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        MockitoAnnotations.initMocks(this);
        TestHelper helper = new TestHelper();
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema-9000-9002.xml");
        servlet = new SolrFusionServlet();
        servlet.setCfg(cfg);
    }

    @Test
    public void testInit()
    {
        doReturn("test-fusion-schema-9000-9002.xml").when(servletConfig).getInitParameter(
            SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA);
        doReturn("configuration.xsd").when(servletConfig).getInitParameter(
            SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA_XSD);
        try
        {
            servlet.init(servletConfig);
        }
        catch (Exception e)
        {
            Assert.fail("Expected no exception, but caught one: " + e);
        }
    }

    @Test
    public void testInitNoXsd()
    {
        doReturn("test-fusion-schema-9000-9002.xml").when(servletConfig).getInitParameter(
            SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA);
        doReturn(null).when(servletConfig).getInitParameter(SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA_XSD);
        try
        {
            servlet.init(servletConfig);
        }
        catch (Exception e)
        {
            Assert.fail("Expected no exception, but caught one: " + e);
        }
    }

    @Test
    public void testInitNoFusionSchema()
    {
        doReturn(null).when(servletConfig).getInitParameter(SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA);
        doReturn(null).when(servletConfig).getInitParameter(SolrFusionServlet.INIT_PARAM_FUSION_SCHEMA_XSD);
        try
        {
            servlet.init(servletConfig);
            Assert.fail("Expected exception for missing fusion schema, but got none");
        }
        catch (Exception e)
        {
            match(e.getMessage(), SolrFusionServlet.ERROR_MSG_FUSION_SCHEMA_FILE_NOT_CONFIGURED);
        }
    }

    @Test
    public void testBuildFusionRequest()
    {
        testBuildFusionRequestImpl(null);
        testBuildFusionRequestImpl("title:Goethe");
    }

    protected void testBuildFusionRequestImpl(String fq)
    {
        SolrFusionServlet servlet = spy(this.servlet);
        Configuration cfg = mock(Configuration.class);
        doReturn(10).when(cfg).getDefaultPageSize();
        doReturn("score desc").when(cfg).getDefaultSortField();
        servlet.setCfg(cfg);
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(QUERY.getRequestParamName(), new String[]{q});
        if (fq != null)
        {
            requestParams.put(FILTER_QUERY.getRequestParamName(), new String[]{fq});
        }
        String fieldsToReturn = "* score";
        requestParams.put(FIELDS_TO_RETURN.getRequestParamName(), new String[]{fieldsToReturn});
        requestParams.put(QUERY_TYPE.getRequestParamName(), new String[]{"morelikethis"});
        requestParams.put(HIGHLIGHT.getRequestParamName(), new String[]{"true"});
        requestParams.put(HIGHLIGHT_PRE.getRequestParamName(), new String[]{"pre"});
        requestParams.put(HIGHLIGHT_POST.getRequestParamName(), new String[]{"post"});
        requestParams.put(HIGHLIGHT_FIELDS_TO_RETURN.getRequestParamName(), new String[]{fieldsToReturn});
        requestParams.put(HIGHLIGHT_QUERY.getRequestParamName(), new String[]{q});
        requestParams.put(FACET.getRequestParamName(), new String[]{"true"});
        requestParams.put(FACET_FIELD.getRequestParamName(),
            new String[]{"{!ex=format_filter}format", "{!ex=format_de15_filter}format_de15"});
        requestParams.put(FACET_LIMIT.getRequestParamName(), new String[]{"20"});
        requestParams.put(FACET_MINCOUNT.getRequestParamName(), new String[]{"2"});
        requestParams.put(FACET_PREFIX.getRequestParamName(), new String[]{"p1"});
        requestParams.put(FACET_SORT.getRequestParamName(), new String[]{"author"});
        requestParams.put("f.finc_class_facet.facet.sort", new String[]{"s1"});
        requestParams.put("f.format.facet.sort", new String[]{"s2"});

        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());

        // check core params
        Assert.assertNotNull("Expected request object", req);
        Assert.assertEquals("Got different query", q, req.getQuery().getValue());
        if (fq == null)
        {
            Assert.assertNull("Expected no filter query list", req.getFilterQuery());
        }
        else
        {
            Assert.assertEquals("Got different filter query", fq, req.getFilterQuery().get(0).getValue());
        }
        Assert.assertEquals("Got different renderer type than expected", ResponseRendererType.JSON,
            req.getResponseType());
        Assert.assertEquals("Got different fields", fieldsToReturn, req.getFieldsToReturn().getValue());
        Assert.assertEquals("Got different query type", "morelikethis", req.getQueryType().getValue());
        Assert.assertFalse("Expected no exception, but got " + req.buildErrorMessage(), req.hasErrors());

        // check highlighting params
        Assert.assertEquals("Got different highlight query", q, req.getHighlightQuery().getValue());
        Assert.assertEquals("Got different highlight", "true", req.getHighlight().getValue());
        Assert.assertEquals("Got different highlight pre", "pre", req.getHighlightPre().getValue());
        Assert.assertEquals("Got different highlight post", "post", req.getHighlightPost().getValue());
        Assert.assertEquals("Got different highlight fields", fieldsToReturn,
            req.getHighlightingFieldsToReturn().getValue());

        // check facet params
        Assert.assertEquals("Got different facet", "true", req.getFacet().getValue());
        Assert.assertEquals("Got different facet", "20", req.getFacetLimit().getValue());
        Assert.assertEquals("Got different facet", "2", req.getFacetMincount().getValue());
        Assert.assertEquals("Got different facet", "p1", req.getFacetPrefix().getValue());
        Assert.assertEquals("Got different facet", "author", req.getFacetSort().getValue());
        List<SolrFusionRequestParam> facetFields = req.getFacetFields();
        Assert.assertNotNull("Expected facet fields", facetFields);
        Assert.assertEquals("Got different number of facet fields", 2, facetFields.size());
        Assert.assertEquals("Got different first facet field", "{!ex=format_filter}format",
            facetFields.get(0).getValue());
        Assert.assertEquals("Got different second facet field", "{!ex=format_de15_filter}format_de15",
            facetFields.get(1).getValue());
        List<SolrFusionRequestParam> facetSortFields = req.getFacetSortFields();
        Assert.assertNotNull("Expected facet sort fields", facetSortFields);
        Assert.assertEquals("Got different number of facet sort fields", 2, facetSortFields.size());
        Assert.assertEquals("Got different first facet sort field value", "s1", facetSortFields.get(0).getValue());
        Assert.assertEquals("Got different second facet sort field value", "s2", facetSortFields.get(1).getValue());
        Assert.assertEquals("Got different first facet sort field", "finc_class_facet",
            facetSortFields.get(0).getParamNameVariablePart());
        Assert.assertEquals("Got different second facet sort field", "format",
            facetSortFields.get(1).getParamNameVariablePart());
    }

    @Test
    public void testBuildFusionRequestWithoutQuery()
    {
        Map<String, String[]> requestParams = new HashMap<>();
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        match(req.buildErrorMessage(), SolrFusionServlet.ERROR_MSG_FOUND_NO_QUERY_PARAMETER,
            SolrFusionRequestParams.QUERY.getRequestParamName());
    }

    @Test
    public void testBuildFusionRequestWithTooManyQueries()
    {
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put(SolrFusionRequestParams.QUERY.getRequestParamName(), new String[]{"schiller", "goethe"});
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        match(req.buildErrorMessage(), SolrFusionServlet.ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS,
            SolrFusionRequestParams.QUERY.getRequestParamName(), "2");
    }

    @Test
    public void testBuildFusionRequestWithTooManyWt()
    {
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put(SolrFusionRequestParams.QUERY.getRequestParamName(), new String[]{"schiller"});
        requestParams.put(SolrFusionRequestParams.WRITER_TYPE.getRequestParamName(), new String[]{"xml", "json"});
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        match(req.buildErrorMessage(), SolrFusionServlet.ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS,
            SolrFusionRequestParams.WRITER_TYPE.getRequestParamName(), "2");
    }

    @Test
    public void testBuildFusionRequestWithKnownRenderer()
    {
        SolrFusionServlet servlet = spy(this.servlet);
        Configuration cfg = mock(Configuration.class);
        doReturn(10).when(cfg).getDefaultPageSize();
        doReturn("score desc").when(cfg).getDefaultSortField();
        servlet.setCfg(cfg);
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(SolrFusionRequestParams.QUERY.getRequestParamName(), new String[]{q});
        String formats[] = {"json", "xml", "php"};
        for (String f : formats)
        {
            requestParams.put(SolrFusionRequestParams.WRITER_TYPE.getRequestParamName(), new String[]{f});
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.assertNotNull("Expected request object", req);
            Assert.assertEquals("Got different different", q, req.getQuery().getValue());
            Assert.assertEquals("Got different renderer type than expected",
                ResponseRendererType.valueOf(f.toUpperCase()), req.getResponseType());
            Assert.assertFalse("Expected no exception, but got " + req.buildErrorMessage(), req.hasErrors());
        }
    }

    @Test
    public void testBuildFusionRequestWithUnknownRenderer()
    {
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(SolrFusionRequestParams.QUERY.getRequestParamName(), new String[]{q});
        requestParams.put(SolrFusionRequestParams.WRITER_TYPE.getRequestParamName(), new String[]{"xyz"});
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        Assert.assertEquals("Found different error message than expected",
            "ERROR: Found no renderer for given type 'XYZ'. Cause: No enum constant org.outermedia.solrfusion.configuration.ResponseRendererType.XYZ\n",
            req.buildErrorMessage());
    }

    protected void match(String actual, String format, Object... args)
    {
        format = format.replace("(", "\\(");
        format = format.replace(")", "\\)");
        format = format.replace("{}", "(.+)");
        format = format.replace("%s", "(.+)");
        format = format.replace("%d", "(.+)");
        System.out.println("PAT " + format + " " + actual);
        Pattern pat = Pattern.compile(format);
        Matcher mat = pat.matcher(actual);
        Assert.assertTrue("Expected match of pattern=" + format + " and value=" + actual, mat.find());
        for (int i = 0; i < args.length; i++)
        {
            Assert.assertEquals("Expected other value", args[i], mat.group(i + 1));
        }
    }

    @Test
    public void testConfigReload() throws ServletException, IOException, InterruptedException
    {
        TestSolrFusionServlet servlet = new TestSolrFusionServlet();
        GregorianCalendar currentCal = new GregorianCalendar(2014, 6, 3, 12, 0, 0);
        long startTime = currentCal.getTimeInMillis();
        servlet.currentTime = startTime;
        File tmpSchema = File.createTempFile("test-schema-", ".xml", new File("target/test-classes"));
        FileUtils.copyFile(new File("target/test-classes/test-fusion-schema.xml"), tmpSchema);
        FileUtils.touch(tmpSchema);
        servlet.loadSolrFusionConfig(tmpSchema.getName(), false);
        Configuration oldCfg = servlet.getCfg();
        Assert.assertNotNull("Solr Fusion schema not loaded", oldCfg);

        // try to re-load immediately, which should be done after 5 minutes only
        log.info("----1");
        servlet.loadSolrFusionConfig(tmpSchema.getName(), false);
        Assert.assertEquals("Solr Fusion schema re-loaded", oldCfg, servlet.getCfg());

        // 6 minutes in the future, without schema modifications, nothing should happen
        currentCal.add(Calendar.MINUTE, 6);
        servlet.currentTime = currentCal.getTimeInMillis();
        log.info("----2");
        servlet.loadSolrFusionConfig(tmpSchema.getName(), false);
        Assert.assertEquals("Solr Fusion schema re-loaded", oldCfg, servlet.getCfg());

        // 6 minutes in the future with schema modifications
        servlet.setSolrFusionSchemaLoadTime(startTime); // modified after loadSolrFusionConfig() call
        Thread.sleep(1001); // await new time
        FileUtils.touch(tmpSchema);
        log.info("----3");
        servlet.loadSolrFusionConfig(tmpSchema.getName(), false);
        Assert.assertNotSame("Solr Fusion schema not re-loaded", oldCfg, servlet.getCfg());
        oldCfg = servlet.getCfg();

        // force re-load
        currentCal.add(Calendar.MINUTE, 1);
        servlet.currentTime = currentCal.getTimeInMillis();
        log.info("----4");
        servlet.loadSolrFusionConfig(tmpSchema.getName(), false);
        Assert.assertEquals("Solr Fusion schema re-loaded", oldCfg, servlet.getCfg());
        log.info("----5");
        servlet.loadSolrFusionConfig(tmpSchema.getName(), true);
        Assert.assertNotSame("Solr Fusion schema not re-loaded", oldCfg, servlet.getCfg());
    }

    @Test
    public void testPageSizeSortDefaulting() throws ServletException
    {
        Configuration cfg = new Configuration();
        servlet.setCfg(cfg);
        cfg.setDefaultSortField("auto asc");
        GlobalSearchServerConfig searchConfig = new GlobalSearchServerConfig();
        cfg.setSearchServerConfigs(searchConfig);
        searchConfig.setDefaultPageSize(23);
        Map<String, Object> headerValues = new HashMap<>();
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put("q", new String[]{"*:*"});
        FusionRequest fusionRequest = servlet.buildFusionRequest(requestParams, headerValues);
        Assert.assertEquals("Expected other start value", "0", fusionRequest.getStart().getValue());
        Assert.assertEquals("Expected other rows value", String.valueOf(searchConfig.getDefaultPageSize()),
            fusionRequest.getPageSize().getValue());
        Assert.assertEquals("Expected other sort field", "auto", fusionRequest.getFusionSortField());
        Assert.assertTrue("Expected other sort dir", fusionRequest.isSortAsc());

        // check desc sort direction
        cfg.setDefaultSortField("mobile desc");
        fusionRequest = servlet.buildFusionRequest(requestParams, headerValues);
        Assert.assertEquals("Expected other sort field", "mobile", fusionRequest.getFusionSortField());
        Assert.assertFalse("Expected other sort dir", fusionRequest.isSortAsc());
    }

    @Test
    public void testBuildPrintableParams()
    {
        Configuration cfg = new Configuration();
        servlet.setCfg(cfg);
        Map<String, Object> map = new HashMap<>();
        map.put("k1", new String[]{"v1"});
        map.put("k2", "v2");
        String s = servlet.buildPrintableParamMap(map);
        Assert.assertEquals("Expected other string", "{\n" +
            "\tk1=[v1]\n" +
            "\tk2=v2\n" +
            "}", s);
    }

    @Test
    public void testErrorHandling()
        throws ServletException, IOException, ParserConfigurationException, JAXBException, SAXException
    {
        SolrFusionServlet servlet = spy(this.servlet);
        doNothing().when(servlet).loadSolrFusionConfig(anyString(), anyBoolean());
        HttpServletRequest req = mock(HttpServletRequest.class);

        // no q param
        String outStr = runRequest(new HashMap<String, String[]>(), servlet, req);
        System.out.println("OUT1 " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr,
            outStr.contains("\"msg\":\"ERROR: Found no query parameter (q)\\n\","));

        // unknown field in qm in json
        Map<String, String[]> reqParams = new HashMap<>();
        reqParams.put("q", new String[]{"xyz:3"});
        outStr = runRequest(reqParams, servlet, req);
        // System.out.println("OUT " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr, outStr.contains(
            "\"msg\":\"Query parsing failed: xyz:3;\\nCause: ERROR: Parsing of query xyz:3 failed.\\nCannot interpret query 'xyz:3': Didn't find field 'xyz' in fusion schema. Please define it there.\\nDidn't find field 'xyz' in fusion schema. Please define it there.\\n\\n\","));

        // unknown field in qm in xml
        reqParams = new HashMap<>();
        reqParams.put("q", new String[]{"xyz:3"});
        reqParams.put("wt", new String[]{"xml"});
        outStr = runRequest(reqParams, servlet, req);
        System.out.println("OUT2 " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr,
            outStr.contains("<str name=\"msg\"><![CDATA[Query parsing failed: xyz:3;\n" +
                "Cause: ERROR: Parsing of query xyz:3 failed.\n" +
                "Cannot interpret query 'xyz:3': Didn't find field 'xyz' in fusion schema. Please define it there.\n" +
                "Didn't find field 'xyz' in fusion schema. Please define it there.\n" +
                "\n]]></str>"));
    }

    protected String runRequest(Map<String, String[]> reqParams, SolrFusionServlet servlet, HttpServletRequest req)
        throws IOException
    {
        doReturn(new HashMap<>()).when(servlet).collectHeader(req);
        doReturn(reqParams).when(req).getParameterMap();
        HttpServletResponse res = mock(HttpServletResponse.class);
        StringBuilderWriter out = new StringBuilderWriter();
        PrintWriter pw = new PrintWriter(out);
        doReturn(pw).when(res).getWriter();
        servlet.doGet(req, res);
        ArgumentCaptor<String> msgArg = ArgumentCaptor.forClass(String.class);
        verify(res).sendError(eq(400), msgArg.capture());
        return msgArg.getValue();
    }
}
