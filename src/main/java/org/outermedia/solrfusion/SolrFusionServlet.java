package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.ResponseRendererIfc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

@Slf4j
@Getter
@Setter
public class SolrFusionServlet extends HttpServlet
{
    protected static final String HEADER_LOCALE = "_LOCALE";
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;
    private final static long FIVE_MIN_IN_MILLIS = 1000 * 60 * 5;
    public static String ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS = "Found too many query parameters (%s). Expected only 1, but got %d";
    public static String ERROR_MSG_FOUND_NO_QUERY_PARAMETER = "Found no query parameter (%s)";
    public static String ERROR_MSG_FUSION_SCHEMA_FILE_NOT_CONFIGURED = "Fusion schema file not configured.";
    public static String INIT_PARAM_FUSION_SCHEMA = "fusion-schema";
    public static String INIT_PARAM_FUSION_SCHEMA_XSD = "fusion-schema-xsd";
    // TODO static cfg, so that all servlets share the same config?
    private Configuration cfg;
    private String fusionXsdFileName;
    private long lastModifiedOfLoadedSolrFusionSchema = 0L;
    private long solrFusionSchemaLoadTime = 0L;
    private String fusionSchemaFileName;

    static
    {
        Util xmlUtil = new Util();
        File absolutePathToLogConfig = new File(xmlUtil.findXmlInClasspath("log4j.properties"));
        PropertyConfigurator.configureAndWatch(absolutePathToLogConfig.getAbsolutePath(), FIVE_MIN_IN_MILLIS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /**
     * Main distribution servlet. Queries are prepared for configured solr instances and their responses are collected
     * and transformed according to the defined logical schema.
     * <p/>
     * The HTTP request parameter <b>forceSchemaReload</b>=&lt;any value&gt; forces an immediate re-load of the
     * solrfusion schema file.
     *
     * @param request  the received http get request which contains a solr query
     * @param response the answer is a typical solr response.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // check for modifications
        loadSolrFusionConfig(fusionSchemaFileName, request.getParameter("forceSchemaReload") != null);
        // set encoding/content type BEFORE getWriter() is called!
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> headerValues = collectHeader(request);
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (log.isDebugEnabled())
        {
            log.debug("Received request:\nHeader:\n{}\nParams:\n{}", buildPrintableParamMap(headerValues),
                buildPrintableParamMap(parameterMap));
        }
        FusionRequest fusionRequest = buildFusionRequest(parameterMap, headerValues);
        ResponseRendererType rendererType = fusionRequest.getResponseType();
        log.debug("Sending back response in {}", rendererType.toString());
        response.setContentType(rendererType.getMimeType());

        PrintWriter pw = response.getWriter();
        FusionResponse fusionResponse = getNewFusionResponse();
        processRequest(response, fusionRequest, fusionResponse, rendererType);
        String responseStr = fusionResponse.getResponseAsString();
        if (responseStr != null && fusionResponse.isOk())
        {
            log.debug("Returning:\n{}", responseStr);
            response.setStatus(200);
            pw.println(responseStr);
        }
        else
        {
            if (responseStr == null)
            {
                String errorMessage = fusionResponse.getErrorMessage();
                if (errorMessage == null)
                {
                    errorMessage = "unknown";
                }
                log.error("Returning error, but no response:\n{}", errorMessage);
                response.sendError(400, errorMessage);
            }
            else
            {
                log.error("Returning error:\n{}", responseStr);
                response.setStatus(400);
                pw.println(responseStr);
            }
        }
    }

    private void processRequest(HttpServletResponse response, FusionRequest fusionRequest,
        FusionResponse fusionResponse, ResponseRendererType rendererType) throws IOException
    {
        if (!fusionRequest.hasErrors())
        {
            callController(fusionRequest, fusionResponse);
        }
        else
        {
            fusionResponse.setError(fusionRequest.buildErrorMessage(), null);
        }
        if (!fusionResponse.isOk() && fusionResponse.getResponseAsString() == null)
        {
            try
            {
                ResponseRendererIfc responseRenderer = cfg.getResponseRendererByType(rendererType);
                if (responseRenderer != null)
                {
                    String responseStr = responseRenderer.getResponseString(cfg, null, fusionRequest, fusionResponse);
                    fusionResponse.setErrorResponse(responseStr);
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while creating error response", e);
            }
        }
    }

    protected String buildPrintableParamMap(Map<String, ?> params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (params != null)
        {
            for (String paramName : params.keySet())
            {
                Object paramValue = params.get(paramName);
                String s;
                if (paramValue.getClass().isArray())
                {
                    s = Arrays.toString((Object[]) paramValue);
                }
                else
                {
                    s = String.valueOf(paramValue);
                }
                sb.append("\t");
                sb.append(paramName);
                sb.append("=");
                sb.append(s);
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    protected Map<String, Object> collectHeader(HttpServletRequest request)
    {
        Map<String, Object> headerValues = new HashMap<>();
        Enumeration<String> headerNameEnum = request.getHeaderNames();
        if (headerNameEnum != null)
        {
            while (headerNameEnum.hasMoreElements())
            {
                String headerName = headerNameEnum.nextElement();
                headerValues.put(headerName, request.getHeader(headerName));
            }
        }
        headerValues.put(HEADER_LOCALE, request.getLocale());
        return headerValues;
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        fusionSchemaFileName = config.getInitParameter(INIT_PARAM_FUSION_SCHEMA);
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
        fusionXsdFileName = config.getInitParameter(INIT_PARAM_FUSION_SCHEMA_XSD);
        if (fusionXsdFileName == null)
        {
            log.warn("Found no servlet init parameter for '{}'. Can't validate fusion schema.",
                INIT_PARAM_FUSION_SCHEMA_XSD);
        }
        loadSolrFusionConfig(fusionSchemaFileName, false);
    }

    protected void loadSolrFusionConfig(String fusionSchemaFileName, boolean force)
    {
        Util xmlUtil = new Util();
        try
        {
            long now = getCurrentTimeInMillis();
            if (force || (now - solrFusionSchemaLoadTime) >= FIVE_MIN_IN_MILLIS)
            {
                File absolutePathToFusionSchema = new File(xmlUtil.findXmlInClasspath(fusionSchemaFileName));
                long lastModified = absolutePathToFusionSchema.lastModified();
                if (force || lastModifiedOfLoadedSolrFusionSchema != lastModified)
                {
                    cfg = xmlUtil.unmarshal(Configuration.class, fusionSchemaFileName, fusionXsdFileName);
                    lastModifiedOfLoadedSolrFusionSchema = lastModified;
                }
                else
                {
                    log.info("Solr Fusion " + absolutePathToFusionSchema + " not modified.");
                }
                solrFusionSchemaLoadTime = now;
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while reading '{}'", fusionSchemaFileName, e);
        }
    }

    protected long getCurrentTimeInMillis()
    {
        return System.currentTimeMillis();
    }

    protected FusionResponse getNewFusionResponse()
    {
        return new FusionResponse();
    }

    protected FusionRequest buildFusionRequest(Map<String, String[]> requestParams, Map<String, Object> headerValues)
    {
        FusionRequest fusionRequest = getNewFusionRequest();

        fusionRequest.setQuery(getRequiredSingleSearchParamValue(requestParams, QUERY, fusionRequest));
        fusionRequest.setFilterQuery(
            getOptionalSingleSearchParamValue(requestParams, FILTER_QUERY, null, fusionRequest));
        // TODO configure default response type in solrfusion schema?
        fusionRequest.setResponseTypeFromString(
            getOptionalSingleSearchParamValue(requestParams, WRITER_TYPE, "json", fusionRequest), fusionRequest);
        Locale sentLocale = (Locale) headerValues.get(HEADER_LOCALE);
        if (sentLocale == null)
        {
            sentLocale = Locale.GERMAN;
        }
        fusionRequest.setLocale(sentLocale);
        String startStr = getOptionalSingleSearchParamValue(requestParams, START, "0", fusionRequest);
        fusionRequest.setStart(parseInt(startStr, 0));
        int defaultPageSize = cfg.getDefaultPageSize();
        String pageSizeStr = getOptionalSingleSearchParamValue(requestParams, PAGE_SIZE,
            String.valueOf(defaultPageSize), fusionRequest);
        fusionRequest.setPageSize(parseInt(pageSizeStr, defaultPageSize));
        String sortStr = getOptionalSingleSearchParamValue(requestParams, SORT, cfg.getDefaultSortField(),
            fusionRequest);
        // "<SPACE> desc" in the case a field's name contains "desc" too
        // because sortStr is trimmed a single "desc" would be treated right
        boolean sortAsc = !sortStr.contains(" desc");
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        fusionRequest.setSolrFusionSortField(st.nextToken());
        fusionRequest.setSortAsc(sortAsc);
        String fieldsToReturn = getOptionalSingleSearchParamValue(requestParams, FIELDS_TO_RETURN, null, fusionRequest);
        fusionRequest.setFieldsToReturn(fieldsToReturn);
        String highLightFieldsToReturn = getOptionalSingleSearchParamValue(requestParams, HIGHLIGHT_FIELDS_TO_RETURN,
            null, fusionRequest);
        fusionRequest.setHighlightingFieldsToReturn(highLightFieldsToReturn);

        return fusionRequest;
    }

    protected int parseInt(String s, int defaultValue)
    {
        int result = defaultValue;
        try
        {
            result = Integer.parseInt(s);
            if (result < 0)
            {
                log.error("Ignoring negative start '{}'. Using {} instead.", s, defaultValue);
                result = defaultValue;
            }
        }
        catch (Exception e)
        {
            result = defaultValue;
            log.error("Couldn't parse '{}' to int. Using {} instead.", s, defaultValue);
        }
        return result;
    }

    protected String getRequiredSingleSearchParamValue(Map<String, String[]> requestParams,
        SolrFusionRequestParams searchParamName, FusionRequest fusionRequest)
    {
        String requestParamName = searchParamName.getRequestParamName();
        String[] qs = requestParams.get(requestParamName);
        if (qs == null || qs.length == 0)
        {
            fusionRequest.addError(buildErrorMessage(ERROR_MSG_FOUND_NO_QUERY_PARAMETER, requestParamName));
            return null;
        }
        if (qs.length > 1)
        {
            fusionRequest.addError(
                buildErrorMessage(ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS, requestParamName, qs.length));
            return null;
        }
        return qs[0].trim();
    }

    protected String getOptionalSingleSearchParamValue(Map<String, String[]> requestParams,
        SolrFusionRequestParams searchParamName, String defaultValue, FusionRequest fusionRequest)
    {
        String s = defaultValue;
        String requestParamName = searchParamName.getRequestParamName();
        String[] qs = requestParams.get(requestParamName);
        if (qs != null)
        {
            if (qs.length == 1)
            {
                s = qs[0].trim();
            }
            else if (qs.length > 1)
            {
                fusionRequest.addError(
                    buildErrorMessage(ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS, requestParamName, qs.length));
                return null;
            }
        }
        return s;
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

    protected void callController(FusionRequest fusionRequest, FusionResponse fusionResponse)
    {
        try
        {
            FusionControllerIfc fc = cfg.getController();
            fc.process(cfg, fusionRequest, fusionResponse);
        }
        catch (Exception e)
        {
            log.error("Caught exception while processing request: " + fusionRequest, e);
            // TODO collect causes too!
            fusionResponse.setError(e.getMessage(), null);
        }
    }
}
