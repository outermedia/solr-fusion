package org.outermedia.solrfusion.adapter.solr;

import junit.framework.Assert;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.XMLResponseWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.SolrServerDualTestBase;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

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

    private InputStream embeddedQueryToXmlInputStream(SolrParams request, QueryResponse response) {
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

    @Test
    public void testMockAdapter() throws SolrServerException, SAXException, JAXBException, ParserConfigurationException, FileNotFoundException {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.addField("title");
        query.addField("author");
        query.addField("id");
        QueryResponse response = firstServer.query(query);
        InputStream inputStream = embeddedQueryToXmlInputStream(query, response);

        ResponseParserIfc responseParser = DefaultResponseParser.Factory.getInstance();
        XmlResponse xmlResponse = responseParser.parse(inputStream);

        Assert.assertEquals("Expected one document", 1, xmlResponse.getResult().getNumFound());
        String expected = "[Document(solrSingleValuedFields=[SolrSingleValuedField(value=1, term=Term(fusionFieldName=null, fusionFieldValue=null, fusionField=null, searchServerFieldName=id, searchServerFieldValue=1, removed=false, wasMapped=false, newTerms=null)), SolrSingleValuedField(value=Shakespeare, term=Term(fusionFieldName=null, fusionFieldValue=null, fusionField=null, searchServerFieldName=author, searchServerFieldValue=Shakespeare, removed=false, wasMapped=false, newTerms=null))], solrMultiValuedFields=[SolrMultiValuedField(values=[Troilus und Cressida], terms=[Term(fusionFieldName=null, fusionFieldValue=null, fusionField=null, searchServerFieldName=title, searchServerFieldValue=Troilus und Cressida, removed=false, wasMapped=false, newTerms=null)])])]";
        Assert.assertEquals("Expected one document", expected, xmlResponse.getResult().getDocuments().toString());
    }
}