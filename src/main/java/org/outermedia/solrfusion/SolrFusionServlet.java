package org.outermedia.solrfusion;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SolrFusionServlet extends HttpServlet
{
	/**
	 * Default serialization id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Main distribution servlet. Queries are prepared for configured solr
	 * instances and their responses are collected and transformed according to
	 * the defined logical schema.
	 * 
	 * @param request the received http get request which contains a solr query
	 * @param response the answer is a typical solr response.
	 **/
	@Override
	protected void doGet(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException
	{
		PrintWriter pw = response.getWriter();
		pw.println("OK");
	}
}
