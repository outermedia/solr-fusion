package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.Util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Map;

@Slf4j
@Getter
@Setter
public class SolrFusionServlet extends HttpServlet
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    public static String ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS = "Found too many query parameters (%s). Expected only 1, but got %d";
    public static String ERROR_MSG_FOUND_NO_QUERY_PARAMETER = "Found no query parameter (%s)";
    public static String ERROR_MSG_FUSION_SCHEMA_FILE_NOT_CONFIGURED = "Fusion schema file not configured.";

    public static String INIT_PARAM_FUSION_SCHEMA = "fusion-schema";
    public static String INIT_PARAM_FUSION_SCHEMA_XSD = "fusion-schema-xsd";

    public static final String SEARCH_PARAM_QUERY = "q";

    private Configuration cfg;


    /**
     * Main distribution servlet. Queries are prepared for configured solr
     * instances and their responses are collected and transformed according to
     * the defined logical schema.
     *
     * @param request  the received http get request which contains a solr query
     * @param response the answer is a typical solr response.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        // set encoding/content type BEFORE getWriter() is called!
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        FusionRequest fusionRequest = buildFusionRequest(request.getParameterMap());
        FusionResponse fusionResponse = getNewFusionResponse();
        String responseStr = process(fusionRequest, fusionResponse);
        pw.println(responseStr);
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        String fusionSchemaFileName = config.getInitParameter(INIT_PARAM_FUSION_SCHEMA);
        if (fusionSchemaFileName == null)
        {
            log.error("Didn't find value for servlet init parameter {}. Please add something like:\n    <servlet>\n" +
                    "        <servlet-name>SolrFusionServlet</servlet-name>\n" +
                    "        <servlet-class>org.outermedia.solrfusion.SolrFusionServlet</servlet-class>\n" +
                    "        <init-param>\n" +
                    "            <param-name>fusion-schema</param-name>\n" +
                    "            <param-value>fusion-schema-uni-leipzig.xml</param-value>\n" +
                    "        </init-param>\n" +
                    "        <init-param>\n" +
                    "            <param-name>fusion-schema-xsd</param-name>\n" +
                    "            <param-value>configuration.xsd</param-value>\n" +
                    "        </init-param>\n" +
                    "    </servlet>", INIT_PARAM_FUSION_SCHEMA);
            throw new ServletException(ERROR_MSG_FUSION_SCHEMA_FILE_NOT_CONFIGURED);
        }
        String fusionXsdFileName = config.getInitParameter(INIT_PARAM_FUSION_SCHEMA_XSD);
        if (fusionXsdFileName == null)
        {
            log.warn("Found no servlet init parameter for '{}'. Can't validate fusion schema.", INIT_PARAM_FUSION_SCHEMA_XSD);
        }
        Util xmlUtil = new Util();
        try
        {
            cfg = xmlUtil.unmarshal(Configuration.class, fusionSchemaFileName,
                    fusionXsdFileName);
        }
        catch (Exception e)
        {
            log.error("Caught exception while reading '{}'", fusionSchemaFileName, e);
            throw new ServletException(e);
        }
    }

    protected FusionResponse getNewFusionResponse()
    {
        return new FusionResponse();
    }

    protected FusionRequest buildFusionRequest(Map<String, String[]> requestParams) throws ServletException
    {
        FusionRequest fusionRequest = getNewFusionRequest();
        fusionRequest.setResponseType(ResponseRendererType.XML);
        String[] qs = requestParams.get(SEARCH_PARAM_QUERY);
        if (qs == null || qs.length == 0)
        {
            throw new ServletException(buildErrorMessage(ERROR_MSG_FOUND_NO_QUERY_PARAMETER, SEARCH_PARAM_QUERY));
        }
        if (qs.length > 1)
        {
            throw new ServletException(buildErrorMessage(ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS, SEARCH_PARAM_QUERY, qs.length));
        }
        fusionRequest.setQuery(qs[0]);
        return fusionRequest;
    }

    protected String buildErrorMessage(String format, Object... args)
    {
        Formatter fmt = new Formatter();
        return fmt.format(format, args).toString();
    }

    protected FusionRequest getNewFusionRequest()
    {
        return new FusionRequest();
    }

    protected String process(FusionRequest fusionRequest, FusionResponse fusionResponse)
    {
        FusionController fc = new FusionController(cfg);
        fc.process(fusionRequest, fusionResponse);
        return fusionResponse.getResponseAsString();
    }
}
