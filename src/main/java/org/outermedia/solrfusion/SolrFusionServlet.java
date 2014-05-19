package org.outermedia.solrfusion;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SolrFusionServlet extends HttpServlet
{
	/**
	 * Main distribution servlet. Queries are prepared
	 * for configured solr instances and their responses
	 * are collected and transformed according to the
	 * defined logical schema.
	 *
	 * @param request the received http get request which 
	 * 	contains a solr query
	 * @param response the answer is a typical solr
	 * 	response.
	 **/
	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
		PrintWriter pw = response.getWriter();
		pw.println("OK");
	}
}
