package org.outermedia.solrfusion;

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
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
        requestParams.put(HIGHLIGHT.getRequestParamName(), new String[]{"true"});
        requestParams.put(HIGHLIGHT_PRE.getRequestParamName(), new String[]{"pre"});
        requestParams.put(HIGHLIGHT_POST.getRequestParamName(), new String[]{"post"});
        requestParams.put(HIGHLIGHT_FIELDS_TO_RETURN.getRequestParamName(), new String[]{fieldsToReturn});
        requestParams.put(HIGHLIGHT_QUERY.getRequestParamName(), new String[]{q});
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        Assert.assertNotNull("Expected request object", req);
        Assert.assertEquals("Got different query", q, req.getQuery());
        Assert.assertEquals("Got different filter query", fq, req.getFilterQuery());
        Assert.assertEquals("Got different renderer type than expected", ResponseRendererType.JSON,
            req.getResponseType());
        Assert.assertEquals("Got different fields", fieldsToReturn, req.getFieldsToReturn());
        Assert.assertFalse("Expected no exception, but got " + req.buildErrorMessage(), req.hasErrors());
        // check highlighting params
        Assert.assertEquals("Got different highlight query", q, req.getHighlightQuery());
        Assert.assertEquals("Got different highlight", "true", req.getHighlight());
        Assert.assertEquals("Got different highlight pre", "pre", req.getHighlightPre());
        Assert.assertEquals("Got different highlight post", "post", req.getHighlightPost());
        Assert.assertEquals("Got different highlight fields", fieldsToReturn, req.getHighlightingFieldsToReturn());
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
    public void testBuildFusionRequestWithTooManyFilterQueries()
    {
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put(SolrFusionRequestParams.QUERY.getRequestParamName(), new String[]{"schiller"});
        requestParams.put(SolrFusionRequestParams.FILTER_QUERY.getRequestParamName(),
            new String[]{"schiller", "goethe"});
        FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
        match(req.buildErrorMessage(), SolrFusionServlet.ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS,
            SolrFusionRequestParams.FILTER_QUERY.getRequestParamName(), "2");
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
            Assert.assertEquals("Got different different", q, req.getQuery());
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
        // System.out.println("PAT " + format);
        Pattern pat = Pattern.compile(format);
        Matcher mat = pat.matcher(actual);
        Assert.assertTrue("Expected match of pattern=" + format + " and value=" + actual, mat.find());
        for (int i = 0; i < args.length; i++)
        {
            Assert.assertEquals("", args[i], mat.group(i + 1));
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
        Assert.assertEquals("Expected other start value", 0, fusionRequest.getStart());
        Assert.assertEquals("Expected other rows value", searchConfig.getDefaultPageSize(),
            fusionRequest.getPageSize());
        Assert.assertEquals("Expected other sort field", "auto", fusionRequest.getSolrFusionSortField());
        Assert.assertTrue("Expected other sort dir", fusionRequest.isSortAsc());

        // check desc sort direction
        cfg.setDefaultSortField("mobile desc");
        fusionRequest = servlet.buildFusionRequest(requestParams, headerValues);
        Assert.assertEquals("Expected other sort field", "mobile", fusionRequest.getSolrFusionSortField());
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
        // System.out.println("OUT " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr,
            outStr.contains("\"msg\":\"ERROR: Found no query parameter (q)\\n\","));

        // unknown field in qm in json
        Map<String, String[]> reqParams = new HashMap<>();
        reqParams.put("q", new String[]{"xyz:3"});
        outStr = runRequest(reqParams, servlet, req);
        // System.out.println("OUT " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr, outStr.contains(
            "\"msg\":\"Query parsing failed: xyz:3\\nCause: ERROR: Parsing of query xyz:3 failed.\\nCannot interpret query 'xyz:3': Didn't find field 'xyz' in fusion schema. Please define it there.\\nDidn't find field 'xyz' in fusion schema. Please define it there.\\n\\n\","));

        // unknown field in qm in xml
        reqParams = new HashMap<>();
        reqParams.put("q", new String[]{"xyz:3"});
        reqParams.put("wt", new String[]{"xml"});
        outStr = runRequest(reqParams, servlet, req);
        System.out.println("OUT " + outStr);
        Assert.assertTrue("Expected error message. but got:\n" + outStr,
            outStr.contains("<str name=\"msg\"><![CDATA[Query parsing failed: xyz:3\n" +
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
        return out.toString();
    }
}
