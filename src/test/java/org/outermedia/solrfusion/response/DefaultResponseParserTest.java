package org.outermedia.solrfusion.response;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * @author stephan
 */
public class DefaultResponseParserTest
{

    protected Util xmlUtil;

    @Before
    public void setup()
    {
        xmlUtil = new Util();
    }

    @Test
    public void testReadResponse()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-1.xml", null);

        List<Document> documents = response.getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10, documents.size());
        Assert.assertEquals("Got different numFound than expected", 59612, response.getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResultName());
        Assert.assertEquals("Got different sourceid in document1 than expected", "beuth_pn_DE30029154",
            documents.get(2).findFieldByName("sourceid").getFirstSearchServerFieldValue());
        Assert.assertEquals("Expected 12  singlevalued fields", 12,
            documents.get(3).getSolrSingleValuedFields().size());
    }

    @Test
    public void testReadResponse9000()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9000.xml", null);

        List<Document> documents = response.getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10, documents.size());
        Assert.assertEquals("Got different numFound than expected", 23121, response.getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResultName());
        Assert.assertEquals("Expected 22 singlevalued fields", 22, documents.get(0).getSolrSingleValuedFields().size());
        Assert.assertEquals("Expected 41 multivalued fields", 41, documents.get(0).getSolrMultiValuedFields().size());
        Assert.assertEquals("Got a different multivalued field than unexpected ", "[DE-15, DE-Ch1]",
            documents.get(0).findFieldByName("institution").getAllSearchServerFieldValue().toString());

    }

    @Test
    public void testReadResponse9001()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9001.xml", null);

        List<Document> documents = response.getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10, documents.size());
        Assert.assertEquals("Got different numFound than expected", 91373, response.getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResultName());
        Assert.assertEquals("Expected 22 singlevalued fields", 16, documents.get(0).getSolrSingleValuedFields().size());
        Assert.assertEquals("Expected 41 multivalued fields", 32, documents.get(0).getSolrMultiValuedFields().size());
        Assert.assertEquals("Got a different multivalued field than unexpected ", "[findex.gbv.de]",
            documents.get(0).findFieldByName("institution").getAllSearchServerFieldValue().toString());

    }

    @Test
    public void testReadResponse9002()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9002.xml", null);

        List<Document> documents = response.getDocuments();

        Assert.assertEquals("Got less result documents than expected", 10, documents.size());
        Assert.assertEquals("Got different numFound than expected", 54, response.getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResultName());
        Assert.assertEquals("Expected 22 singlevalued fields", 11, documents.get(0).getSolrSingleValuedFields().size());
        Assert.assertNull("Expected MultiValuedFields to be null", documents.get(0).getSolrMultiValuedFields());

    }

    @Test
    public void testReadResponse9003()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "test-xml-response-9003.xml", null);

        List<Document> documents = response.getDocuments();

        Assert.assertEquals("Got less result documents than expected", 2, documents.size());
        Assert.assertEquals("Got different numFound than expected", 2, response.getNumFound());
        Assert.assertEquals("Got attribut value for name@result than expected", "response", response.getResultName());
        Assert.assertEquals("Got different sourceid in document1 than expected", "beuth_pn_DE18954967",
            documents.get(0).findFieldByName("sourceid").getFirstSearchServerFieldValue());
        Assert.assertEquals("Expected 11  singlevalued fields", 11,
            documents.get(1).getSolrSingleValuedFields().size());
        Assert.assertNull("Expected MultiValuedFields to be null", documents.get(0).getSolrMultiValuedFields());
    }

    @Test
    public void testHighlightingParsing()
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException
    {
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "response-with-facets-highlighting.xml", null);
        List<Highlighting> highlighting = response.getHighlighting();
        // System.out.println("R " + highlighting);
        Assert.assertNotNull("Expected parsed highlights", highlighting);
        Assert.assertEquals("Found different number of highlights", 20, highlighting.size());

        Document firstHl = highlighting.get(0).getDocument("id");
        Assert.assertEquals("First document id is different", "0000869567", firstHl.getSearchServerDocId("id"));
        List<String> expected = Arrays.asList("Allerlei über {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}");
        Assert.assertEquals("Expected other first title highlighting", expected,
            firstHl.getSearchServerValuesOf("title"));
        Assert.assertEquals("Expected other first title_short highlighting", expected,
            firstHl.getSearchServerValuesOf("title_short"));
        Assert.assertEquals("Expected other first title_full highlighting", expected,
            firstHl.getSearchServerValuesOf("title_full"));

        Document lastHl = highlighting.get(highlighting.size() - 1).getDocument("id");
        Assert.assertEquals("Last document id is different", "0000971353", lastHl.getSearchServerDocId("id"));
        expected = Arrays.asList(
            "Schriftenreihe der Essener {{{{START_HILITE}}}}Goethe{{{{END_HILITE}}}}-Gesellschaft");
        Assert.assertEquals("Expected other first title highlighting", expected,
            lastHl.getSearchServerValuesOf("title"));
        Assert.assertEquals("Expected other first title_short highlighting", expected,
            lastHl.getSearchServerValuesOf("title_short"));
        Assert.assertEquals("Expected other first title_full highlighting", expected,
            lastHl.getSearchServerValuesOf("title_full"));

        List<FacetHit> facetFields = response.getFacetFields();
        Assert.assertNotNull("Expected parsed facet fields", facetFields);
        Assert.assertEquals("Found different number of facet fields", 3, facetFields.size());
        FacetHit hit1 = facetFields.get(0);
        String expectedDocStr = "UNMAP: branch_de15[25]=not assigned,Bibliotheca Albertina,Deutsches Literaturinstitut Leipzig,Kunst,Musik,Orientwissenschaften,Geographie,Campus-Bibliothek,Sudhoffinstitut,Theaterwissenschaften,Archäologie,Erziehungswissenschaft,Rechtswissenschaft,Chemie/Physik,Orientwissenschaften, Ägyptologie,Geowissenschaften,Veterinärmedizin,Zentralbibliothek Medizin,Mineralogie,Museum für Musikinstrumente,Orientwissenschaften, Altorientalistik,Sportwissenschaft,Biowissenschaften,Theoretische Physik,Zentralbibliothek Medizin 2";
        checkFacetField(hit1, "branch_de15", 25, "not assigned", 18484, "Zentralbibliothek Medizin 2", 1,
            expectedDocStr);
        FacetHit hitLast = facetFields.get(facetFields.size() - 1);
        expectedDocStr = "UNMAP: collcode_de15[3]=Magazin / Sonstige,Freihand,Lehrbuchsammlung";
        checkFacetField(hitLast, "collcode_de15", 3, "Magazin / Sonstige", 8023, "Lehrbuchsammlung", 19,
            expectedDocStr);
    }

    protected void checkFacetField(FacetHit hit, String searchServerField, int wordCount, String firstWord,
        int firstWordCount, String lastWord, int lastWordCount, String expectedDocStr)
    {
        Assert.assertEquals("First search server field is different", searchServerField,
            hit.getSearchServerFieldName());
        List<WordCount> hit1WordCounts = hit.getFieldCounts();
        Assert.assertNotNull("Expected parsed facet field word counts", hit1WordCounts);
        Assert.assertEquals("Found different number of facet field word counts", wordCount, hit1WordCounts.size());
        Assert.assertEquals("Word of first facet hit is different", firstWord, hit1WordCounts.get(0).getWord());
        Assert.assertEquals("Word count of first facet hit is different", firstWordCount,
            hit1WordCounts.get(0).getCount());
        Assert.assertEquals("Word of last facet hit is different", lastWord,
            hit1WordCounts.get(wordCount - 1).getWord());
        Assert.assertEquals("Word count of last facet hit is different", lastWordCount,
            hit1WordCounts.get(wordCount - 1).getCount());
        // System.out.println("DOC " + hit.getDocument().buildFusionDocStr());
        Assert.assertEquals("Got different facet hit document", expectedDocStr,
            hit.getDocument().buildFusionDocStr().trim());
    }

}
