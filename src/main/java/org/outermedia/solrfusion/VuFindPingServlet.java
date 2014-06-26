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
        pw.println("");
    }
}
