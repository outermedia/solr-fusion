package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.BooleanClause;
import org.outermedia.solrfusion.query.parser.BooleanQuery;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.SimpleXmlResponseRenderer;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.AbstractTypeTest;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 6/19/14.
 */
public class DropTest extends AbstractTypeTest
{
    @Test
    public void testDropResponse()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        buildResponseField(doc, "Titel", "Ein kurzer Weg");
        buildResponseField(doc, "Autor", "Willi Schiller");
        buildResponseField(doc, "id", "132");
        Term sourceField = buildResponseField(doc, "f8", "something", "other");
        Term sourceRegExpField = buildResponseField(doc, "f9-abc", "something2", "other2");

        SimpleXmlResponseRenderer renderer = SimpleXmlResponseRenderer.getInstance();
        renderer.setMultiValueKey("arr");
        Map<String, String> map = new HashMap<>();
        map.put("text", "str");
        renderer.setFusionTypeToResponseKey(map);
        List<Document> docs = new ArrayList<>();
        docs.add(doc);
        SearchServerResponseInfo info = new SearchServerResponseInfo(1);

        ScriptEnv env = new ScriptEnv();
        SearchServerConfig serverConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        rm.mapResponse(cfg, serverConfig, doc, env, null);
        // System.out.println(sourceField.toString());
        Assert.assertTrue("Expected that field f8 was removed", sourceField.isRemoved());
        Assert.assertTrue("Expected that field f9-abc was removed", sourceRegExpField.isRemoved());

        ClosableIterator<Document, SearchServerResponseInfo> docStream = new ClosableListIterator<>(docs, info);
        FusionRequest req = new FusionRequest();
        req.setQuery("a:dummy");
        String ds = renderer.getResponseString(cfg, docStream, req, new FusionResponse());
        String expectedField = "    <arr name=\"text4\">\n" +
            "      <str><![CDATA[something]]></str>\n" +
            "      <str><![CDATA[other]]></str>\n" +
            "    </arr>";
        Assert.assertFalse("Field f8 was mapped unexpectedly", ds.contains(expectedField));

        // remove <drop> for f8
        sourceField.resetSearchServerField();
        FieldMapping fm = findByName("f8", serverConfig.getFieldMappings());
        Assert.assertEquals("Found different mapping than expected", "f8", fm.getSearchServersName());
        fm.setFusionName("text4");
        fm.getOperations().clear();

        rm.mapResponse(cfg, serverConfig, doc, env, null);
        Assert.assertFalse("Expected that field f8 was not removed", sourceField.isRemoved());
        // System.out.println("W/O DROP "+sourceField.toString());
        docStream = new ClosableListIterator<>(docs, info);
        String s = renderer.getResponseString(cfg, docStream, req, new FusionResponse());
        Assert.assertTrue("Field f8 was not mapped.", s.contains(expectedField));
    }

    protected FieldMapping findByName(String s, List<FieldMapping> mappings)
    {
        for (FieldMapping fm : mappings)
        {
            if (s.equals(fm.getSearchServersName()))
            {
                return fm;
            }
        }
        return null;
    }

    protected FieldMapping findByFusionName(String s, List<FieldMapping> mappings)
    {
        for (FieldMapping fm : mappings)
        {
            if (s.equals(fm.getFusionName()))
            {
                return fm;
            }
        }
        return null;
    }

    @Test
    public void testDropQuery()
        throws IOException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException,
        IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        QueryMapperIfc rm = QueryMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("text4", "bla1");
        Term term2 = Term.newFusionTerm("text5-abc", "bla2");
        Query query = new TermQuery(term);
        Query query2 = new TermQuery(term2);
        BooleanQuery q = new BooleanQuery();
        q.add(new BooleanClause(query, BooleanClause.Occur.OCCUR_MUST));
        q.add(new BooleanClause(query2, BooleanClause.Occur.OCCUR_MUST));

        ScriptEnv env = new ScriptEnv();
        SearchServerConfig serverConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        qm.mapQuery(cfg, serverConfig, q, env);
        // System.out.println(term.toString());
        Assert.assertTrue("Expected that field text4 was removed", term.isRemoved());
        Assert.assertTrue("Expected that field text5-abc was removed", term2.isRemoved());
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        String ds = qb.buildQueryString(query, cfg, searchServerConfig, Locale.GERMAN);
        // the original query was removed, but two queries are added!
        Assert.assertEquals("Expected no query", "(+t11:\"searched text\"^75.0~) AND t13:hello", ds);

        // remove <drop> for text4
        term.resetQuery();
        FieldMapping fm = findByFusionName("text4", serverConfig.getFieldMappings());
        Assert.assertEquals("Found different mapping than expected", "text4", fm.getFusionName());
        fm.setSearchServersName("f8");
        fm.getOperations().clear();

        qm.mapQuery(cfg, serverConfig, query, env);
        // System.out.println(term.toString());
        Assert.assertFalse("Expected that field text4 was not removed", term.isRemoved());
        String s = qb.buildQueryString(query, cfg, searchServerConfig, Locale.GERMAN);
        Assert.assertEquals("Found different query than expected", "(f8:bla1) AND (+t11:\"searched text\"^75.0~) AND t13:hello", s);
    }

    @Test
    public void testBadDropMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);

        // make bad drop query
        FieldMapping text9Mapping = searchServerConfig.findAllMappingsForFusionField("text9").get(0);
        text9Mapping.setFusionName(null);
        text9Mapping.setMappingType(MappingType.EXACT_NAME_ONLY);
        // System.out.println("BAD DROP " + text9Mapping);
        DropOperation drop = (DropOperation) text9Mapping.getOperations().get(0);
        try
        {
            drop.check(text9Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 279: Invalid configuration: Found <om:drop> without <om:response> or <om:query-response> target.",
                e.getMessage());
        }

        // make bad drop query
        FieldMapping text10Mapping = searchServerConfig.findAllMappingsForFusionField("text10").get(0);
        text10Mapping.setSearchServersName(null);
        text10Mapping.setMappingType(MappingType.EXACT_FUSION_NAME_ONLY);
        // System.out.println("BAD DROP " + text10Mapping);
        drop = (DropOperation) text10Mapping.getOperations().get(0);
        try
        {
            drop.check(text10Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 284: Invalid configuration: Found <om:drop> without <om:query> or <om:query-response> target.",
                e.getMessage());
        }
    }
}
