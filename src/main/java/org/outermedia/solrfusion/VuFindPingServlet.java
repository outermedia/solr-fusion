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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

/**
 * A special servlet used by vufind in order to check the availability of a Solr server.
 */
@Slf4j
@Getter
@Setter
public class VuFindPingServlet extends AbstractServlet
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Main distribution servlet. Queries are prepared for configured solr instances and their responses are collected
     * and transformed according to the defined logical schema.
     *
     * @param request  the received http get request which contains a solr query
     * @param response the answer is a typical solr response.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        if (log.isDebugEnabled())
        {
            Map<String, Object> headerValues = collectHeader(request);
            Map<String, String[]> parameterMap = request.getParameterMap();
            String url = rebuildRequestUrl(request, parameterMap);
            log.debug("Received request: {}\nHeader:\n{}\nParams:\n{}", url, buildPrintableParamMap(headerValues),
                buildPrintableParamMap(parameterMap));
        }

        // set encoding/content type BEFORE getWriter() is called!
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(200);
        PrintWriter pw = response.getWriter();
        StringBuilder sb = new StringBuilder();
        if("all".equals(request.getParameter("echoParams")))
        {
            Enumeration<String> paramEnum = request.getParameterNames();
            if (paramEnum != null)
            {
                while (paramEnum.hasMoreElements())
                {
                    String paramName = paramEnum.nextElement();
                    String paramValue = request.getParameter(paramName);
                    sb.append("\t\t\t<str name=\"" + paramName + "\">" + paramValue + "</str>\n");
                }
            }
        }
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<response>\n" +
            "\t<lst name=\"responseHeader\">\n" +
            "\t\t<int name=\"status\">0</int>\n" +
            "\t\t<int name=\"QTime\">3</int>\n" +
            "\t\t<lst name=\"params\">\n" +
            sb.toString() +
            "\t\t</lst>\n" +
            "\t</lst>\n" +
            "\t<str name=\"status\">OK</str>\n" +
            "</response>");
    }
}
