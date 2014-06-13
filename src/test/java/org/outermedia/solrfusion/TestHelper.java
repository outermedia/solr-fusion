package org.outermedia.solrfusion;

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
