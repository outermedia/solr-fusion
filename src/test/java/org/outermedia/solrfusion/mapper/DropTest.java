package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FieldMapping;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 6/19/14.
 */
public class DropTest extends AbstractTypeTest
{
    @Test
    public void testDropResponse()
            throws IOException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        buildResponseField(doc, "Titel", "Ein kurzer Weg");
        buildResponseField(doc, "Autor", "Willi Schiller");
        buildResponseField(doc, "id", "132");
        Term sourceField = buildResponseField(doc, "f8", "something", "other");

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
        rm.mapResponse(cfg, serverConfig, doc, env);
        // System.out.println(sourceField.toString());
        Assert.assertTrue("Expected that field f8 was removed", sourceField.isRemoved());

        ClosableIterator<Document, SearchServerResponseInfo> docStream = new ClosableListIterator<>(docs, info);
        String ds = renderer.getResponseString(docStream, "a:dummy");
        String expectedField = "    <arr name=\"text4\">\n" +
                "      <str><![CDATA[something]]></str>\n" +
                "      <str><![CDATA[other]]></str>\n" +
                "    </arr>";
        Assert.assertFalse("Field f8 was mapped unexpectedly", ds.contains(expectedField));

        // remove <drop> for f8
        sourceField.resetSearchServerField();
        List<FieldMapping> mappings = serverConfig.getFieldMappings();
        FieldMapping fm = mappings.get(mappings.size() - 2);
        Assert.assertEquals("Found different mapping than expected", "f8", fm.getSearchServersName());
        fm.setFusionName("text4");
        fm.getOperations().clear();

        rm.mapResponse(cfg, serverConfig, doc, env);
        Assert.assertFalse("Expected that field f8 was not removed", sourceField.isRemoved());
        // System.out.println("W/O DROP "+sourceField.toString());
        docStream = new ClosableListIterator<>(docs, info);
        String s = renderer.getResponseString(docStream, "a:dummy");
        Assert.assertTrue("Field f8 was not mapped.", s.contains(expectedField));
    }

    @Test
    public void testDropQuery()
            throws IOException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        QueryMapperIfc rm = QueryMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("text4", "bla1");
        Query query = new TermQuery(term);

        ScriptEnv env = new ScriptEnv();
        SearchServerConfig serverConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        qm.mapQuery(cfg, serverConfig, query, env);
        // System.out.println(term.toString());
        Assert.assertTrue("Expected that field text4 was removed", term.isRemoved());
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        String ds = qb.buildQueryString(query);
        Assert.assertEquals("Expected no query", "", ds);

        // remove <drop> for text4
        term.resetQuery();
        List<FieldMapping> mappings = serverConfig.getFieldMappings();
        FieldMapping fm = mappings.get(mappings.size() - 1);
        Assert.assertEquals("Found different mapping than expected", "text4", fm.getFusionName());
        fm.setSearchServersName("f8");
        fm.getOperations().clear();

        qm.mapQuery(cfg, serverConfig, query, env);
        // System.out.println(term.toString());
        Assert.assertFalse("Expected that field text4 was not removed", term.isRemoved());
        String s = qb.buildQueryString(query);
        Assert.assertEquals("Found different query than expected", "f8:bla1", s);
    }

}
