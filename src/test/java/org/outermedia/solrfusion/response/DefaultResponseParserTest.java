package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XMLResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author stephan
 */
public class DefaultResponseParserTest {

    protected Util xmlUtil;

    @Before
    public void setup()
    {
        xmlUtil = new Util();
    }

    @Test
    public void testReadResponse() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        XMLResponse response = xmlUtil.unmarshal(XMLResponse.class, "test-xml-response-1.xml", null);

        List<Document> documents = response.getResult().getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10,  documents.size());
        Assert.assertEquals("Got different numFound than expected", 59612, response.getResult().getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResult().getResultName());
        Assert.assertEquals("Got different sourceid in document1 than expected", "beuth_pn_DE30029154", documents.get(2).findFieldByName("sourceid").getFirstSearchServerFieldValue());
        Assert.assertEquals("Expected 12  singlevalued fields", 12, documents.get(3).getSolrSingleValuedFields().size());
    }
    @Test
    public void testReadResponse9000() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        XMLResponse response = xmlUtil.unmarshal(XMLResponse.class, "test-xml-response-9000.xml", null);

        List<Document> documents = response.getResult().getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10,  documents.size());
        Assert.assertEquals("Got different numFound than expected", 23121, response.getResult().getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResult().getResultName());
        Assert.assertEquals("Expected 22 singlevalued fields", 22, documents.get(0).getSolrSingleValuedFields().size());
        Assert.assertEquals("Expected 41 multivalued fields", 41, documents.get(0).getSolrMultiValuedFields().size());
        Assert.assertEquals("Got a different multivalued field than unexpected ", "[DE-15, DE-Ch1]", documents.get(0).findFieldByName("institution").getAllSearchServerFieldValue().toString());

    }
    @Test
    public void testReadResponse9003() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        XMLResponse response = xmlUtil.unmarshal(XMLResponse.class, "test-xml-response-9003.xml", null);

        List<Document> documents = response.getResult().getDocuments();

        Assert.assertEquals("Got less result documents than expected", 2,  documents.size());
        Assert.assertEquals("Got different numFound than expected", 2, response.getResult().getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResult().getResultName());
        Assert.assertEquals("Got different sourceid in document1 than expected", "beuth_pn_DE18954967", documents.get(0).findFieldByName("sourceid").getFirstSearchServerFieldValue());
        Assert.assertEquals("Expected 11  singlevalued fields", 11, documents.get(1).getSolrSingleValuedFields().size());
    }

}
