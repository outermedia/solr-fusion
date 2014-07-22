package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.SimpleXmlResponseRenderer;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.types.AbstractTypeTest;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/**
 * Created by ballmann on 7/18/14.
 */
public class AddTest extends AbstractTypeTest
{
    @Test
    public void testBadAddMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);

        // make bad add query
        FieldMapping text11Mapping = searchServerConfig.findAllMappingsForFusionField("text11").get(0);
        text11Mapping.setSearchServersName(null);
        text11Mapping.setMappingType(MappingType.EXACT_FUSION_NAME_ONLY);
        // System.out.println("BAD ADD " + text9Mapping);
        AddOperation add = (AddOperation) text11Mapping.getOperations().get(0);
        try
        {
            add.check(text11Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 291: Please specify a field for attribute 'name' in order to add something to a query.",
                e.getMessage());
        }

        // make bad add response
        FieldMapping text12Mapping = searchServerConfig.findAllMappingsForFusionField("text12").get(0);
        text12Mapping.setFusionName(null);
        text12Mapping.setMappingType(MappingType.EXACT_NAME_ONLY);
        // System.out.println("BAD ADD " + text10Mapping);
        add = (AddOperation) text12Mapping.getOperations().get(0);
        try
        {
            add.check(text12Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 298: Please specify a field for attribute 'fusion-name' in order to add something to a response.",
                e.getMessage());
        }

        // TODO check for missing type attribute -> Error
    }

    @Test
    public void testAddResponse()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        Document doc = createDocument("id", "123", "score", "1.2");
        ScriptEnv env = new ScriptEnv();
        int mappedFieldNr = cfg.getResponseMapper().mapResponse(cfg, searchServerConfig, doc, env, null);
        SimpleXmlResponseRenderer renderer = (SimpleXmlResponseRenderer) cfg.getResponseRendererByType(
            ResponseRendererType.XML);
        StringBuilder sb = new StringBuilder();
        FieldVisitor visitor = renderer.getDocumentFieldVisitor(sb);
        doc.accept(visitor, new ScriptEnv());
        String xmlDocStr = sb.toString();
        // System.out.println("DOC "+xmlDocStr);
        Assert.assertTrue("Expected field text12 set" + xmlDocStr,
            xmlDocStr.contains("<str name=\"text12\"><![CDATA[1]]></str>"));
        Assert.assertTrue("Expected field text13 set" + xmlDocStr,
            xmlDocStr.contains("    <arr name=\"text13\">\n" +
                "      <str><![CDATA[1]]></str>\n" +
                "      <str><![CDATA[2]]></str>\n" +
                "      <str><![CDATA[3]]></str>\n" +
                "    </arr>"));
    }

    protected Document createDocument(String... fieldAndValue)
    {
        Document doc = new Document();
        for (int i = 0; i < fieldAndValue.length; i += 2)
        {
            doc.addField(fieldAndValue[i], fieldAndValue[i + 1]);
        }
        return doc;
    }

    @Test
    public void testAddQueryOutside()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        TermQuery q = new TermQuery(Term.newFusionTerm("title", "abc123"));
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(q, cfg, searchServerConfig, Locale.GERMAN);
        // System.out.println("QS "+qs);
        String expected = "(Titel:abc123) AND (+t11:\"searched text\"^75.0~) AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddQueryInsideWithChange()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        Term term = Term.newFusionTerm("text14", "abc123");
        term.setFusionField(cfg.findFieldByName("text14"));
        TermQuery q = new TermQuery(term);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(q, cfg, searchServerConfig, Locale.GERMAN);
        System.out.println("QS "+qs);
        String expected = "((t14:hello0 OR t14a:helloA OR t14b:helloB)) AND (+t11:\"searched text\"^75.0~) AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddQueryInsideWithDrop()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        Term term = Term.newFusionTerm("text15", "abc123");
        term.setFusionField(cfg.findFieldByName("text15"));
        TermQuery q = new TermQuery(term);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(q, cfg, searchServerConfig, Locale.GERMAN);
        System.out.println("QS "+qs);
        String expected = "((t15a:helloA OR t15b:helloB)) AND (+t11:\"searched text\"^75.0~) AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }
}
