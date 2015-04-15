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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.TextResponseRendererIfc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This class (re)loads SolrFusion's schema definition, creates a FusionRequest object for the FusionController and
 * evaluates the FusionResponse in order to return a Solr response.
 */
@Slf4j
@Getter
@Setter
public class SolrFusionServlet extends AbstractServlet
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
    public static String INIT_PARAM_APPLY_LATIN1_FIX = "applyLatin1Fix";

    // TODO static cfg, so that all servlets share the same config?
    private Configuration cfg;
    private String fusionXsdFileName;
    private long lastModifiedOfLoadedSolrFusionSchema = 0L;
    private long solrFusionSchemaLoadTime = 0L;
    private String fusionSchemaFileName;
    private boolean applyLatin1Fix = false;

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
     * @param request
     *     the received http get request which contains a solr query
     * @param response
     *     the answer is a typical solr response.
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
            String url = rebuildRequestUrl(request, parameterMap);
            log.debug("Received request: {}\nHeader:\n{}\nParams:\n{}", url, buildPrintableParamMap(headerValues),
                buildPrintableParamMap(parameterMap));
        }
        long startTime = System.currentTimeMillis();
        FusionRequest fusionRequest = buildFusionRequest(parameterMap, headerValues);
        ResponseRendererType rendererType = fusionRequest.getResponseType();
        log.debug("Sending back response in {}", rendererType.toString());
        response.setContentType(rendererType.getMimeType());

        FusionResponse fusionResponse = getNewFusionResponse(response);
        fusionResponse.setQStart(startTime);
        startTime = System.currentTimeMillis();
        processRequest(response, fusionRequest, fusionResponse, rendererType);
        if (fusionResponse.isWroteSomeData() && fusionResponse.isOk())
        {
            response.setStatus(200);
            long endTime = System.currentTimeMillis();
            log.info("Wrote response in {}ms", endTime - startTime);
        }
        else if (!fusionResponse.isWroteSomeData())
        {
            // if no content was written, try to send an error and an empty content
            String errorMessage = fusionResponse.getErrorMessage();
            if (errorMessage == null)
            {
                errorMessage = "unknown";
            }
            // try to create empty answer if a text response is expected
            try
            {
                ResponseRendererIfc responseRenderer = cfg.getResponseRendererByType(rendererType);
                if (responseRenderer != null && (responseRenderer instanceof TextResponseRendererIfc))
                {
                    StringWriter emptyResponseWriter = new StringWriter();
                    PrintWriter textWriter = new PrintWriter(emptyResponseWriter);
                    fusionResponse.setTextWriter(textWriter);
                    responseRenderer.writeResponse(cfg, null, fusionRequest, fusionResponse);
                    if (fusionResponse.isWroteSomeData())
                    {
                        textWriter.flush();
                        errorMessage = emptyResponseWriter.toString();
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while creating error response", e);
            }
            log.error("Returning error, but no response:\n{}", errorMessage);
            response.sendError(400, errorMessage);
        }
    }

    protected void processRequest(HttpServletResponse response, FusionRequest fusionRequest,
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
        applyLatin1Fix = "true".equals(config.getInitParameter(INIT_PARAM_APPLY_LATIN1_FIX));
        log.info("Will use latin1 fix: {}", applyLatin1Fix);
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

    protected FusionResponse getNewFusionResponse(HttpServletResponse servletResponse)
    {
        FusionResponse r = new FusionResponse();
        r.setServletResponse(servletResponse);
        return r;
    }

    protected FusionRequest buildFusionRequest(Map<String, String[]> requestParams, Map<String, Object> headerValues)
    {
        FusionRequest fusionRequest = getNewFusionRequest();

        buildCoreFusionRequest(requestParams, headerValues, fusionRequest);

        buildHighlightFusionRequest(requestParams, fusionRequest);

        buildFacetFusionRequest(requestParams, fusionRequest);

        return fusionRequest;
    }

    protected void buildCoreFusionRequest(Map<String, String[]> requestParams, Map<String, Object> headerValues,
        FusionRequest fusionRequest)
    {
        fusionRequest.setQuery(getRequiredSingleSearchParamValue(requestParams, QUERY, fusionRequest));
        fusionRequest.setFilterQuery(getOptionalMultiSearchParamValue(requestParams, FILTER_QUERY));
        // TODO configure default response type in solrfusion schema?
        fusionRequest.setResponseTypeFromString(
            getOptionalSingleSearchParamValue(requestParams, WRITER_TYPE, "json", fusionRequest), fusionRequest);
        Locale sentLocale = (Locale) headerValues.get(HEADER_LOCALE);
        if (sentLocale == null)
        {
            sentLocale = Locale.GERMAN;
        }
        fusionRequest.setLocale(sentLocale);
        SolrFusionRequestParam startParam = getOptionalSingleSearchParamValue(requestParams, START, "0", fusionRequest);
        fusionRequest.setStart(startParam);
        int defaultPageSize = cfg.getDefaultPageSize();
        SolrFusionRequestParam pageSizeParam = getOptionalSingleSearchParamValue(requestParams, PAGE_SIZE,
            String.valueOf(defaultPageSize), fusionRequest);
        fusionRequest.setPageSize(pageSizeParam);
        SolrFusionRequestParam sortParam = getOptionalSingleSearchParamValue(requestParams, SORT,
            cfg.getDefaultSortField(), fusionRequest);
        fusionRequest.setSort(sortParam);
        fusionRequest.setSortSpec(fusionRequest.setSolrFusionSortingFromString(sortParam));
        SolrFusionRequestParam fieldsToReturn = getOptionalSingleSearchParamValue(requestParams, FIELDS_TO_RETURN, null,
            fusionRequest);
        fusionRequest.setFieldsToReturn(fieldsToReturn);
        SolrFusionRequestParam queryType = getOptionalSingleSearchParamValue(requestParams, QUERY_TYPE, null,
            fusionRequest);
        fusionRequest.setQueryType(queryType);
        SolrFusionRequestParam queryFields = getOptionalSingleSearchParamValue(requestParams, QUERY_FIELD, null,
            fusionRequest);
        fusionRequest.setQueryFields(queryFields);
        SolrFusionRequestParam minimumMatch = getOptionalSingleSearchParamValue(requestParams, MINIMUM_MATCH, null,
            fusionRequest);
        fusionRequest.setMinimumMatch(minimumMatch);
        SolrFusionRequestParam omitHeader = getOptionalSingleSearchParamValue(requestParams, OMIT_HEADER, null,
            fusionRequest);
        fusionRequest.setOmitHeader(omitHeader);
        SolrFusionRequestParam ids = getOptionalSingleSearchParamValue(requestParams, IDS, null, fusionRequest);
        fusionRequest.setIds(ids);
    }

    protected void buildFacetFusionRequest(Map<String, String[]> requestParams, FusionRequest fusionRequest)
    {
        SolrFusionRequestParam facet = getOptionalSingleSearchParamValue(requestParams, FACET, null, fusionRequest);
        fusionRequest.setFacet(facet);
        if (facet != null && "true".equals(facet.getValue()))
        {
            SolrFusionRequestParam facetMinCount = getOptionalSingleSearchParamValue(requestParams, FACET_MINCOUNT,
                null, fusionRequest);
            fusionRequest.setFacetMincount(facetMinCount);
            SolrFusionRequestParam facetLimit = getOptionalSingleSearchParamValue(requestParams, FACET_LIMIT, null,
                fusionRequest);
            fusionRequest.setFacetLimit(facetLimit);
            SolrFusionRequestParam facetSort = getOptionalSingleSearchParamValue(requestParams, FACET_SORT, null,
                fusionRequest);
            fusionRequest.setFacetSort(facetSort);
            SolrFusionRequestParam facetPrefix = getOptionalSingleSearchParamValue(requestParams, FACET_PREFIX, null,
                fusionRequest);
            fusionRequest.setFacetPrefix(facetPrefix);
            List<SolrFusionRequestParam> facetFields = getOptionalMultiSearchParamValue(requestParams, FACET_FIELD);
            fusionRequest.setFacetFields(facetFields);
            List<SolrFusionRequestParam> facetSortFields = getOptionalMultiSearchParamValue(requestParams,
                FACET_SORT_FIELD);
            fusionRequest.setFacetSortFields(facetSortFields);
        }
    }

    protected void buildHighlightFusionRequest(Map<String, String[]> requestParams, FusionRequest fusionRequest)
    {
        SolrFusionRequestParam highlight = getOptionalSingleSearchParamValue(requestParams, HIGHLIGHT, null,
            fusionRequest);
        fusionRequest.setHighlight(highlight);
        if (highlight != null && "true".equals(highlight.getValue()))
        {
            SolrFusionRequestParam highlightFieldsToReturn = getOptionalSingleSearchParamValue(requestParams,
                HIGHLIGHT_FIELDS_TO_RETURN, null, fusionRequest);
            fusionRequest.setHighlightingFieldsToReturn(highlightFieldsToReturn);
            SolrFusionRequestParam highlightPost = getOptionalSingleSearchParamValue(requestParams, HIGHLIGHT_POST,
                null, fusionRequest);
            fusionRequest.setHighlightPost(highlightPost);
            SolrFusionRequestParam highlightPre = getOptionalSingleSearchParamValue(requestParams, HIGHLIGHT_PRE, null,
                fusionRequest);
            fusionRequest.setHighlightPre(highlightPre);
            SolrFusionRequestParam highlightQuery = getOptionalSingleSearchParamValue(requestParams, HIGHLIGHT_QUERY,
                null, fusionRequest);
            fusionRequest.setHighlightQuery(highlightQuery);
        }
    }

    protected SolrFusionRequestParam getRequiredSingleSearchParamValue(Map<String, String[]> requestParams,
        SolrFusionRequestParams searchParamName, FusionRequest fusionRequest)
    {
        SolrFusionRequestParam result = getOptionalSingleSearchParamValue(requestParams, searchParamName, null,
            fusionRequest);
        if (result == null || result.getValue() == null)
        {
            fusionRequest.addError(
                buildErrorMessage(ERROR_MSG_FOUND_NO_QUERY_PARAMETER, searchParamName.getRequestParamName()));
            return new SolrFusionRequestParam();
        }
        return result;
    }

    protected SolrFusionRequestParam getOptionalSingleSearchParamValue(Map<String, String[]> requestParams,
        SolrFusionRequestParams searchParamName, String defaultValue, FusionRequest fusionRequest)
    {
        List<SolrFusionRequestParam> result = getRequestParamByName(searchParamName, requestParams);
        SolrFusionRequestParam oneResult = null;
        if (result == null)
        {
            oneResult = new SolrFusionRequestParam(null, defaultValue);
        }
        else if (!result.isEmpty())
        {
            if (result.size() > 1)
            {
                String requestParamName = searchParamName.getRequestParamName();
                fusionRequest.addError(
                    buildErrorMessage(ERROR_MSG_FOUND_TOO_MANY_QUERY_PARAMETERS, requestParamName, result.size()));
                return new SolrFusionRequestParam();
            }
            oneResult = result.get(0);
        }
        return oneResult;
    }

    protected List<SolrFusionRequestParam> getRequestParamByName(SolrFusionRequestParams reqParam,
        Map<String, String[]> requestParams)
    {
        List<SolrFusionRequestParam> result = null;
        if (reqParam.isPattern())
        {
            result = new ArrayList<>();
            for (Map.Entry<String, String[]> rp : requestParams.entrySet())
            {
                String key = fixLatin1Encoding(rp.getKey());
                String patternValue = reqParam.matches(key);
                if (patternValue != null)
                {
                    String[] params = rp.getValue();
                    for (int i = 0; i < params.length; i++)
                    {
                        result.add(new SolrFusionRequestParam(fixLatin1Encoding(params[i]), patternValue, null));
                    }
                }
            }
            if (result.isEmpty())
            {
                result = null;
            }
        }
        else
        {
            String[] params = requestParams.get(reqParam.getRequestParamName());
            if (params != null)
            {
                result = new ArrayList<>();
                for (int i = 0; i < params.length; i++)
                {
                    result.add(new SolrFusionRequestParam(fixLatin1Encoding(params[i]), null, null));
                }
            }
        }
        return result;
    }

    protected String fixLatin1Encoding(String s)
    {
        if (applyLatin1Fix)
        {
            try
            {
                s = new String(s.getBytes("ISO-8859-1"));
            }
            catch (UnsupportedEncodingException e)
            {
                log.error("Caught encoding exception. Can't fix encoding of: " + s, e);
            }
        }
        return s;
    }

    protected List<SolrFusionRequestParam> getOptionalMultiSearchParamValue(Map<String, String[]> requestParams,
        SolrFusionRequestParams searchParamName)
    {
        return getRequestParamByName(searchParamName, requestParams);
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
            FusionControllerIfc fc;
            SolrFusionRequestParam ids = fusionRequest.getIds();
            if (ids.getValue() != null)
            {
                // TODO should be configurable in schema
                fc = new IdsFusionController();
                fc.init(null);
            }
            else
            {
                fc = cfg.getController();
            }
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
