package org.outermedia.solrfusion;

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.doReturn;

@Slf4j
public class SolrFusionServletTest
{
    static class TestSolrFusionServlet extends SolrFusionServlet
    {
        protected long currentTime;

        @Override protected long getCurrentTimeInMillis()
        {
            return currentTime;
        }
    }

    @Mock
    ServletConfig servletConfig;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
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
        SolrFusionServlet servlet = new SolrFusionServlet();
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
        SolrFusionServlet servlet = new SolrFusionServlet();
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
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_QUERY, new String[]{q});
        try
        {
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.assertNotNull("Expected request object", req);
            Assert.assertEquals("Got different different", q, req.getQuery());
            Assert.assertEquals("Got different renderer type than expected", ResponseRendererType.XML,
                req.getResponseType());
        }
        catch (ServletException e)
        {
            Assert.fail("Expected no exception, but got " + e);
        }
    }

    @Test
    public void testBuildFusionRequestWithoutQuery()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        try
        {
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.fail("Expected exception, but got none");
        }
        catch (ServletException e)
        {
            match(e.getMessage(), SolrFusionServlet.ERROR_MSG_FOUND_NO_QUERY_PARAMETER,
                SolrFusionServlet.SEARCH_PARAM_QUERY);
        }
    }

    @Test
    public void testBuildFusionRequestWithTooManyQueries()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_QUERY, new String[]{"schiller", "goethe"});
        try
        {
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.fail("Expected exception, but got none");
        }
        catch (ServletException e)
        {
            match(e.getMessage(), SolrFusionServlet.ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS,
                SolrFusionServlet.SEARCH_PARAM_QUERY, "2");
        }
    }

    @Test
    public void testBuildFusionRequestWithTooManyWt()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_QUERY, new String[]{"schiller"});
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_WT, new String[]{"xml", "json"});
        try
        {
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.fail("Expected exception, but got none");
        }
        catch (ServletException e)
        {
            match(e.getMessage(), SolrFusionServlet.ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS,
                SolrFusionServlet.SEARCH_PARAM_WT, "2");
        }
    }

    @Test
    public void testBuildFusionRequestWithKnownRenderer()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_QUERY, new String[]{q});
        String formats[] = {"json", "xml", "php"};
        for (String f : formats)
        {
            requestParams.put(SolrFusionServlet.SEARCH_PARAM_WT, new String[]{f});
            try
            {
                FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
                Assert.assertNotNull("Expected request object", req);
                Assert.assertEquals("Got different different", q, req.getQuery());
                Assert.assertEquals("Got different renderer type than expected",
                    ResponseRendererType.valueOf(f.toUpperCase()),
                    req.getResponseType());
            }
            catch (ServletException e)
            {
                Assert.fail("Expected no exception, but got " + e);
            }
        }
    }

    @Test
    public void testBuildFusionRequestWithUnknownRenderer()
    {
        SolrFusionServlet servlet = new SolrFusionServlet();
        Map<String, String[]> requestParams = new HashMap<>();
        String q = "title:schiller";
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_QUERY, new String[]{q});
        requestParams.put(SolrFusionServlet.SEARCH_PARAM_WT, new String[]{"xyz"});
        try
        {
            FusionRequest req = servlet.buildFusionRequest(requestParams, new HashMap<String, Object>());
            Assert.fail("Expected exception, but got none");
        }
        catch (ServletException e)
        {
            Assert.assertEquals("Found different error message than expected", "Found no renderer for given type 'XYZ'",
                e.getMessage());
        }
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
}
