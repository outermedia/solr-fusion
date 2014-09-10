package org.outermedia.solrfusion.types;

import junit.framework.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrField;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 8/19/14.
 */
public class FieldMergerTest extends AbstractTypeTest
{

    private Configuration cfg;

    @Test
    public void mergeSingleValueFieldsToMultiValue()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        Document doc = buildDocument("author", "a1", "author2", "a2");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("GBV"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        String fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertTrue("Expected to find merged author_facet: " + fusionDocStr,
            fusionDocStr.contains("author_facet[2]=a1,a2"));

        // nothing to merge
        doc = buildDocument("id", "12345");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("GBV"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertFalse("Expected not find author_facet: " + fusionDocStr, fusionDocStr.contains("author_facet"));
    }

    @Test
    public void mergeMultiAndSingleValueFieldsToMultiValue()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        Document doc = buildDocument("author", Arrays.asList("a1a", "a1b"), "author2", "a2");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("GBV"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        String fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertTrue("Expected to find merged author_facet: " + fusionDocStr,
            fusionDocStr.contains("author_facet[3]=a1a,a1b,a2"));
    }

    @Test
    public void mergeSingleValueFieldsToSingleValue()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        Document doc = buildDocument("content", "1", "dbName", "2", "description", "3", "edition", "4", "keywords", "5",
            "publisher", "6", "subjects", "7", "titleVT_de", "8", "titleVT_eng", "9");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("DBoD1"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        String fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertTrue("Expected to find merged allfields: " + fusionDocStr,
            fusionDocStr.contains("allfields=1;2;3;4;5;6;7;8;9"));
    }

    @Test
    public void mergeMultiAndSingleValueFieldsToSingleValue()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        // no value for publisher
        Document doc = buildDocument("content", "a", "dbName", Arrays.asList("b1", "b2"), "description", "c", "edition",
            "d", "keywords", "e", "subjects", Arrays.asList("g1", "g2"), "titleVT_de", "h", "titleVT_eng", "i");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("DBoD1"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        String fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertTrue("Expected to find merged allfields: " + fusionDocStr,
            fusionDocStr.contains("allfields=a;b1;b2;c;d;e;g1;g2;h;i"));
    }

    @Test
    public void splitMergeAndNormalizationTest()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        // no value for publisher
        Document doc = buildDocument("titleVT_de", " ...Ja aber doch", "titleVT_eng", "\"..Aber ja");
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("DBoD1"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        String fusionDocStr = doc.buildFusionDocStr();
        // System.out.println("FDOC " + fusionDocStr);
        Assert.assertTrue("Expected to find normalized merged title_sort: " + fusionDocStr,
            fusionDocStr.contains("title_sort=ja aber doch;\"..aber ja"));
        Assert.assertTrue("Expected to find original merged title: " + fusionDocStr,
            fusionDocStr.contains("title= ...Ja aber doch;\"..Aber ja"));
    }

    @Test
    public void testFacetWordCountMerging()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        ResponseMapperIfc testResponseMapper = cfg.getResponseMapper();
        // the mapping is very incomplete, so ignore all unmapped fields
        testResponseMapper.ignoreMissingMappings();

        Configuration spyCfg = spy(cfg);
        when(spyCfg.getResponseMapper()).thenReturn(testResponseMapper);

        testFacetWordCount(testResponseMapper, spyCfg, Arrays.asList(1, 2), Arrays.asList(3), Arrays.asList(1, 2, 3));
        testFacetWordCount(testResponseMapper, spyCfg, null, Arrays.asList(3), Arrays.asList(1, 1, 3));
        testFacetWordCount(testResponseMapper, spyCfg, Arrays.asList(1, 2), null, Arrays.asList(1, 2, 1));
        testFacetWordCount(testResponseMapper, spyCfg, null, null, null);
    }

    protected void testFacetWordCount(ResponseMapperIfc testResponseMapper, Configuration spyCfg,
        List<Integer> wordCounts1, List<Integer> wordCounts2, List<Integer> expectedWordCounts)
    {
        Document doc = buildDocumentWithFacetWordCounts("author", Arrays.asList("a1a", "a1b"), wordCounts1, "author2",
            "a2", wordCounts2);
        testResponseMapper.mapResponse(spyCfg, cfg.getSearchServerConfigByName("GBV"), doc, new ScriptEnv(), null,
            ResponseTarget.ALL);
        List<Integer> wordCounts = doc.getFusionFacetWordCountsOf("author_facet");
        // System.out.println("FWC " + wordCounts);
        Assert.assertEquals("Expected other merged facet word counts", expectedWordCounts, wordCounts);
    }

    protected Document buildDocument(Object... fields)
    {
        Document doc = new Document();
        for (int i = 0; i < fields.length; i += 2)
        {
            String name = (String) fields[i];
            Object value = fields[i + 1];
            if (value instanceof List)
            {
                doc.addField(name, (List<String>) value);
            }
            else
            {
                doc.addField(name, (String) value);
            }
        }
        return doc;
    }

    protected Document buildDocumentWithFacetWordCounts(Object... fields)
    {
        Document doc = new Document();
        for (int i = 0; i < fields.length; i += 3)
        {
            String name = (String) fields[i];
            Object value = fields[i + 1];
            List<Integer> wordCounts = (List<Integer>) fields[i + 2];
            SolrField sf;
            if (value instanceof List)
            {
                sf = doc.addField(name, (List<String>) value);
            }
            else
            {
                sf = doc.addField(name, (String) value);
            }
            sf.getTerm().setSearchServerFacetCount(wordCounts);
        }
        return doc;
    }
}
