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

import junit.framework.Assert;
import lombok.Getter;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Util;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

@Getter
public class TestHelper
{
	protected Util xmlUtil;

	public TestHelper()
	{
		xmlUtil = new Util();
	}

	public Configuration readFusionSchemaWithoutValidation(String xmlPath)
		throws FileNotFoundException, JAXBException, SAXException,
		ParserConfigurationException
	{
		return readFusionSchemaWithValidation(xmlPath, null);
	}

	public Configuration readFusionSchemaWithValidation(String xmlPath,
		String schemaPath) throws FileNotFoundException, JAXBException,
		SAXException, ParserConfigurationException
	{
		Configuration cfg = xmlUtil.unmarshal(Configuration.class, xmlPath,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ xmlPath, cfg);
		return cfg;
	}

    public static InputStream embeddedQueryToXmlInputStream(SolrParams request, QueryResponse response) {
        XMLResponseWriter xmlWriter = new XMLResponseWriter();
        Writer w = new StringWriter();
        SolrQueryResponse sResponse = new SolrQueryResponse();
        sResponse.setAllValues(response.getResponse());
        try {
            xmlWriter.write(w, new LocalSolrQueryRequest(null, request), sResponse);
        } catch (IOException e) {
            throw new RuntimeException("Unable to convert Solr response into XML", e);
        }
        StringReader stringReader = new StringReader(w.toString());

        ReaderInputStream readerInputStream = new ReaderInputStream(stringReader);
        return readerInputStream;
    }
}
