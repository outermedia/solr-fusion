package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@Slf4j
@Getter
@Setter
public class VuFindPingServlet extends HttpServlet
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
