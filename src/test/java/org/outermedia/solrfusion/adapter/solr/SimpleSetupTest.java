package org.outermedia.solrfusion.adapter.solr;

import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.SolrServerDualTestBase;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by stephan on 03.06.14.
 */
public class SimpleSetupTest extends SolrServerDualTestBase {

    @Before
    public void fillSolr() throws IOException, SolrServerException {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", String.valueOf(1));
        document.addField("title", String.valueOf("Troilus und Cressida"));
        document.addField("author", String.valueOf("Shakespeare"));
        firstServer.add(document);
        firstTestServer.commitLastDocs();
    }

    @After
    public void cleanSolr() throws IOException, SolrServerException {
        firstServer.deleteByQuery("*:*");
    }

    @Test
    public void testMockAdapter() throws SolrServerException, SAXException, JAXBException, ParserConfigurationException, FileNotFoundException {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.addField("title");
        query.addField("author");
        query.addField("id");
        QueryResponse response = firstServer.query(query);
        InputStream inputStream = TestHelper.embeddedQueryToXmlInputStream(query, response);

        ResponseParserIfc responseParser = DefaultResponseParser.Factory.getInstance();
        XmlResponse xmlResponse = responseParser.parse(inputStream);

        Assert.assertEquals("Expected one document", 1, xmlResponse.getResult().getNumFound());
        String expected = "[Document(solrSingleValuedFields=[SolrSingleValuedField(value=1), SolrSingleValuedField(value=Shakespeare)], solrMultiValuedFields=[SolrMultiValuedField(values=[Troilus und Cressida])])]";
        Assert.assertEquals("Expected one document", expected, xmlResponse.getResult().getDocuments().toString());
    }
}