package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.Document;
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
        DefaultResponseParser parser = xmlUtil.unmarshal(DefaultResponseParser.class, "test-xml-response-1.xml", null);

        List<Document> documents = parser.getResult().getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10,  documents.size());
        Assert.assertEquals("Got different numFound than expected", 59612,  parser.getResult().getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", parser.getResult().getResultName());
        Assert.assertEquals("Got different sourceid in document1 than expected", "beuth_pn_DE30029154", documents.get(2).findFieldByName("sourceid").getValue());
        Assert.assertEquals("Expected 1 float element", 1, documents.get(3).getSolrFloatFields().size());
        Assert.assertEquals("Expected 12 string element", 11, documents.get(3).getSolrStringFields().size());
    }
}
