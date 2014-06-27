package org.outermedia.solrfusion;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.configuration.ResponseRendererType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.doReturn;

public class SolrFusionServletTest
{
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
}
