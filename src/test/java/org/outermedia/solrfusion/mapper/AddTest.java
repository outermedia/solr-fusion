package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.DefaultXmlResponseRenderer;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.AbstractTypeTest;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.GregorianCalendar;
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
                "In fusion schema at line 295: Please specify a field for attribute 'name' in order to add something to a query.",
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
                "In fusion schema at line 302: Please specify a field for attribute 'fusion-name' in order to add something to a response.",
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
        DefaultXmlResponseRenderer renderer = DefaultXmlResponseRenderer.Factory.getInstance();
        renderer.init(null);
        FusionRequest req = new FusionRequest();
        req.setQuery("a:dummy");
        req.setLocale(Locale.GERMAN);
        SearchServerResponseInfo info = new SearchServerResponseInfo(1, null);
        ClosableIterator<Document, SearchServerResponseInfo> docStream = new ClosableListIterator<>(Arrays.asList(doc), info);
        String xmlDocStr = renderer.getResponseString(cfg, docStream, req, new FusionResponse());
        System.out.println("DOC "+xmlDocStr);
        Assert.assertTrue("Expected field text12 set" + xmlDocStr,
            xmlDocStr.contains("<str name=\"text12\">1</str>"));
        Assert.assertTrue("Expected field text13 set" + xmlDocStr, xmlDocStr.contains("<arr name=\"text13\">\n" +
            "        <str>1</str>\n" +
            "        <str>2</str>\n" +
            "        <str>3</str>\n" +
            "                </arr>"));
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
        String expected = "(Titel:abc123) AND +t11:\"searched text\"~2^75 AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddQueryInsideWithChange()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        TermQuery q = buildTermQuery(cfg, "text14", "abc123");
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(q, cfg, searchServerConfig, Locale.GERMAN);
        System.out.println("QS " + qs);
        String expected = "((t14:hello0 OR t14a:helloA OR t14b:helloB)) AND +t11:\"searched text\"~2^75 AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddQueryInsideWithDrop()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        TermQuery q = buildTermQuery(cfg, "text15", "abc123");
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(q, cfg, searchServerConfig, Locale.GERMAN);
        System.out.println("QS " + qs);
        String expected = "((t15a:helloA OR t15b:helloB)) AND +t11:\"searched text\"~2^75 AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddOneQueryInsideWithDropAndNoModification()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        String text16 = "text16";
        BooleanQuery bq = buildAllQuery(cfg, text16);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, bq, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN);
        // System.out.println("QS " + qs);
        String expected = "(((s16:\"abc 123\"^1.5) OR (s16:456DEF) OR (s16:fuzzy~)~ OR (s16:pre*fix) OR (s16:wild*) " +
            "OR (s16:[20140801 TO 20140825]) OR (s16:[1.0 TO 4.0]) OR (s16:[1.0 TO 4.0]) OR (s16:[1 TO 4]) " +
            "OR (s16:[1 TO 4]) OR *:*))";
        Assert.assertTrue("Didn't find " + expected + " in " + qs, qs.startsWith(expected));
    }

    protected BooleanQuery buildAllQuery(Configuration cfg, String text16)
    {
        Query tqList[] = {buildPhraseQuery(cfg, text16, "abc 123"), buildTermQuery(cfg, text16,
            "456DEF"), buildFuzzyQuery(cfg, text16, "fuzzy"), buildPrefixQuery(cfg, text16,
            "pre*fix"), buildWildcardQuery(cfg, text16, "wild*"), buildDateRangeQuery(cfg,
            text16), buildDoubleRangeQuery(cfg, text16), buildFloatRangeQuery(cfg, text16), buildIntRangeQuery(cfg,
            text16), buildLongRangeQuery(cfg, text16), buildAllDocsQuery()};
        tqList[0].setBoost(1.5f);
        BooleanQuery bq = new BooleanQuery();
        for (Query tq : tqList)
        {
            bq.add(new BooleanClause(tq, BooleanClause.Occur.OCCUR_SHOULD));
        }
        return bq;
    }

    @Test
    public void testAddMultipleQueryInsideWithDropAndNoModification()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        String field = "text17";
        BooleanQuery bq = buildAllQuery(cfg, field);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, bq, new ScriptEnv());
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN);
        // System.out.println("QS " + qs);
        String expected = "(((s17:\"abc 123\"^1.5 OR s18:\"abc 123\"^1.5) OR (s17:456DEF OR s18:456DEF) " +
            "OR (s17:fuzzy~ OR s18:fuzzy~)~ OR (s17:pre*fix OR s18:pre*fix) OR (s17:wild* OR s18:wild*) " +
            "OR (s17:[20140801 TO 20140825] OR s18:[20140801 TO 20140825]) OR (s17:[1.0 TO 4.0] OR s18:[1.0 TO 4.0]) " +
            "OR (s17:[1.0 TO 4.0] OR s18:[1.0 TO 4.0]) OR (s17:[1 TO 4] OR s18:[1 TO 4]) OR (s17:[1 TO 4] " +
            "OR s18:[1 TO 4]) OR *:*))";
        Assert.assertTrue("Didn't find " + expected + " in " + qs, qs.startsWith(expected));
    }

    protected TermQuery buildTermQuery(Configuration cfg, String fusionField, String fusionValue)
    {
        Term term = Term.newFusionTerm(fusionField, fusionValue);
        term.setFusionField(cfg.findFieldByName(fusionField));
        return new TermQuery(term);
    }

    protected TermQuery buildPhraseQuery(Configuration cfg, String fusionField, String fusionValue)
    {
        Term term = Term.newFusionTerm(fusionField, fusionValue);
        term.setFusionField(cfg.findFieldByName(fusionField));
        return new PhraseQuery(term);
    }

    protected TermQuery buildFuzzyQuery(Configuration cfg, String fusionField, String fusionValue)
    {
        Term term = Term.newFusionTerm(fusionField, fusionValue);
        term.setFusionField(cfg.findFieldByName(fusionField));
        return new FuzzyQuery(term, 3);
    }

    protected TermQuery buildPrefixQuery(Configuration cfg, String fusionField, String fusionValue)
    {
        Term term = Term.newFusionTerm(fusionField, fusionValue);
        term.setFusionField(cfg.findFieldByName(fusionField));
        return new PrefixQuery(term);
    }

    protected TermQuery buildWildcardQuery(Configuration cfg, String fusionField, String fusionValue)
    {
        Term term = Term.newFusionTerm(fusionField, fusionValue);
        term.setFusionField(cfg.findFieldByName(fusionField));
        return new WildcardQuery(term);
    }

    protected TermQuery buildDateRangeQuery(Configuration cfg, String fusionField)
    {
        DateRangeQuery result = new DateRangeQuery(fusionField, new GregorianCalendar(2014, 7, 1),
            new GregorianCalendar(2014, 7, 25), true, true);
        result.getTerm().setFusionField(cfg.findFieldByName(fusionField));
        return result;
    }

    protected TermQuery buildDoubleRangeQuery(Configuration cfg, String fusionField)
    {
        DoubleRangeQuery result = new DoubleRangeQuery(fusionField, 1.0, 4.0, true, true);
        result.getTerm().setFusionField(cfg.findFieldByName(fusionField));
        return result;
    }

    protected TermQuery buildFloatRangeQuery(Configuration cfg, String fusionField)
    {
        FloatRangeQuery result = new FloatRangeQuery(fusionField, 1.0f, 4.0f, true, true);
        result.getTerm().setFusionField(cfg.findFieldByName(fusionField));
        return result;
    }

    protected TermQuery buildIntRangeQuery(Configuration cfg, String fusionField)
    {
        IntRangeQuery result = new IntRangeQuery(fusionField, 1, 4, true, true);
        result.getTerm().setFusionField(cfg.findFieldByName(fusionField));
        return result;
    }

    protected TermQuery buildLongRangeQuery(Configuration cfg, String fusionField)
    {
        LongRangeQuery result = new LongRangeQuery(fusionField, 1L, 4L, true, true);
        result.getTerm().setFusionField(cfg.findFieldByName(fusionField));
        return result;
    }

    protected Query buildAllDocsQuery()
    {
        MatchAllDocsQuery result = new MatchAllDocsQuery();
        return result;
    }
}
