package org.outermedia.solrfusion.mapper;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.DefaultXmlResponseRenderer;
import org.outermedia.solrfusion.response.FacetDocCountBuilder;
import org.outermedia.solrfusion.response.FacetDocCountSorter;
import org.outermedia.solrfusion.response.parser.DocCount;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.AbstractTypeTest;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 7/18/14.
 */
public class AddTest extends AbstractTypeTest
{

    private ScriptEnv env;

    @Test
    public void testBadAddMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);

        // make bad add query
        FieldMapping text11Mapping = searchServerConfig.findAllMappingsForFusionField("text11").get(0).getMapping();
        text11Mapping.setSearchServersName(null);
        text11Mapping.setMappingType(MappingType.EXACT_FUSION_NAME_ONLY);
        // System.out.println("BAD ADD " + text11Mapping);
        AddOperation add = (AddOperation) text11Mapping.getOperations().get(0);
        try
        {
            add.check(text11Mapping);
            Assert.fail("Expected exception for bad add query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 313: Please specify a field for attribute 'name' in order to add something to a query.",
                e.getMessage());
        }

        // make another bad query (missing level)
        FieldMapping text14Mapping = searchServerConfig.findAllMappingsForFusionField("text14").get(0).getMapping();
        add = (AddOperation) text14Mapping.getOperations().get(1);
        add.setLevel(null);
        // System.out.println("BAD ADD " + text11Mapping);
        try
        {
            add.check(text14Mapping);
            Assert.fail("Expected exception for bad add query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 358: Please specify the level attribute when <om:add> is used for queries. Possible values are 'inside' and 'outside'.",
                e.getMessage());
        }

        // make bad add response
        FieldMapping text12Mapping = searchServerConfig.findAllMappingsForFusionField("text12").get(0).getMapping();
        text12Mapping.setFusionName(null);
        text12Mapping.setMappingType(MappingType.EXACT_NAME_ONLY);
        // System.out.println("BAD ADD " + text10Mapping);
        add = (AddOperation) text12Mapping.getOperations().get(0);
        try
        {
            add.check(text12Mapping);
            Assert.fail("Expected exception for bad add query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 320: Please specify a field for attribute 'fusion-name' in order to add something to a response.",
                e.getMessage());
        }
    }

    @Test
    public void testAddResponse()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        Document doc = createDocument("id", "123", "score", "1.2");
        ScriptEnv env = getNewEnv(null, cfg);
        int mappedFieldNr = cfg.getResponseMapper().mapResponse(cfg, searchServerConfig, doc, env, null, ResponseTarget.ALL, true);
        DefaultXmlResponseRenderer renderer = DefaultXmlResponseRenderer.Factory.getInstance();
        renderer.init(null);
        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("a:dummy"));
        req.setLocale(Locale.GERMAN);
        SearchServerResponseInfo info = new SearchServerResponseInfo(1, null, null, null);
        ClosableIterator<Document, SearchServerResponseInfo> docStream = new ClosableListIterator<>(Arrays.asList(doc),
            info);
        FusionResponse fusionResponse = new FusionResponse();
        StringWriter sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        renderer.writeResponse(cfg, docStream, req, fusionResponse);
        String xmlDocStr = sw.toString();
            // System.out.println("DOC "+xmlDocStr);
        Assert.assertTrue("Expected field text12 set in " + xmlDocStr,
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
        ScriptEnv env = getNewEnv("title", cfg);
        q.getTerm().setFusionField((FusionField) env.getBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION));
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, env, null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, q, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.QUERY);
        // System.out.println("QS "+qs);
        String expected = "(Titel:abc123) AND +t11:\"searched text\"~2^75 AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    protected ScriptEnv getNewEnv(String fusionField, Configuration cfg)
    {
        ScriptEnv env = new ScriptEnv();
        if(fusionField != null)
        {
            env.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION, cfg.findFieldByName(fusionField));
        }
        return env;
    }

    @Test
    public void testAddQueryOutsideDismax()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        TermQuery q = new TermQuery(Term.newFusionTerm("title", "abc123"));
        ScriptEnv env = getNewEnv("title", cfg);
        q.getTerm().setFusionField((FusionField) env.getBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION));
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, env, null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDismaxQueryBuilder();
        String qs = buildQueryString(qb, q, cfg, searchServerConfig, Locale.GERMAN, Sets.newHashSet("Titel"), QueryTarget.QUERY);
        // System.out.println("QS "+qs);
        // TODO how can map rules respect dismax and edismax? currently they return the whole query which is wrong
        // for dismax queries
        String expected = "abc123 +t11:\"searched text\"~2^75 t13:hello";
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
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, getNewEnv(null, cfg), null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, q, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.QUERY);
        // System.out.println("QS " + qs);
        String expected = "((t14:hello1 OR t14a:helloA OR t14b:helloB)) AND +t11:\"searched text\"~2^75 AND t13:hello";
        Assert.assertEquals("Got different query than expected", expected, qs);
    }

    @Test
    public void testAddQueryInsideWithChangeForMustQuery()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        TermQuery tq = buildTermQuery(cfg, "text14", "abc123");
        BooleanClause bc = new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST);
        BooleanQuery q = new BooleanQuery();
        q.add(bc);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, getNewEnv(null, cfg), null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, q, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.QUERY);
        System.out.println("QS " + qs);
        String expected = "((+(t14:hello1 OR t14a:helloA OR t14b:helloB))) AND +t11:\"searched text\"~2^75 AND t13:hello";
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
        env = getNewEnv(null, cfg);
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, env, null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, q, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.QUERY);
        // System.out.println("QS " + qs);
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
        ScriptEnv env = getNewEnv(null, cfg);
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.QUERY);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, bq, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.QUERY);
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
        ScriptEnv env = getNewEnv(null, cfg);
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String qs = buildQueryString(qb, bq, cfg, searchServerConfig, Locale.GERMAN, null, QueryTarget.ALL);
        // System.out.println("QS " + qs);
        String expected = "(((s17:\"abc 123\"^1.5 OR s18:\"abc 123\"^1.5) OR (s17:456DEF OR s18:456DEF) " +
            "OR (s17:fuzzy~ OR s18:fuzzy~)~ OR (s17:pre*fix OR s18:pre*fix) OR (s17:wild* OR s18:wild*) " +
            "OR (s17:[20140801 TO 20140825] OR s18:[20140801 TO 20140825]) OR (s17:[1.0 TO 4.0] OR s18:[1.0 TO 4.0]) " +
            "OR (s17:[1.0 TO 4.0] OR s18:[1.0 TO 4.0]) OR (s17:[1 TO 4] OR s18:[1 TO 4]) OR (s17:[1 TO 4] " +
            "OR s18:[1 TO 4]) OR *:*))";
        Assert.assertTrue("Didn't find " + expected + " in " + qs, qs.startsWith(expected));
    }

    protected String buildQueryString(QueryBuilderIfc qb, Query bq, Configuration cfg, SearchServerConfig searchServerConfig,
        Locale locale, Set<String> defaultSearchServerFields, QueryTarget target)
    {
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN, defaultSearchServerFields, target);
        qs = qb.getStaticallyAddedQueries(cfg,searchServerConfig,Locale.GERMAN,target, qs);
        return qs;
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

    @Test
    public void testAddInFacet()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        FusionRequest req = new FusionRequest();
        req.setQuery(new SolrFusionRequestParam("a:dummy"));
        req.setLocale(Locale.GERMAN);

        // map search document
        Document doc = createDocument("id", "123", "score", "1.2", "s19", "abc");
        ScriptEnv env = getNewEnv(null, cfg);
        cfg.getResponseMapper().mapResponse(cfg, searchServerConfig, doc, env, null, ResponseTarget.ALL, true);

        // map facet
        Document facetDoc = new Document();
        facetDoc.addField("s19","a","b","c").getTerm().setSearchServerFacetCount(Arrays.asList(1,2,3));
        facetDoc.setSearchServerDocId(searchServerConfig.getIdFieldName(), "1");
        cfg.getResponseMapper().mapResponse(cfg, searchServerConfig, facetDoc, getNewEnv(null, cfg), null, ResponseTarget.ALL, true);
        Map<String, Map<String, Integer>> facets = new HashMap<>();
        facetDoc.accept(new FacetDocCountBuilder(cfg.getFusionIdFieldName(), cfg.getIdGenerator(), doc, facets), null);
        FacetDocCountSorter facetSorter = new FacetDocCountSorter();
        Map<String, List<DocCount>> sortedFacets = facetSorter.sort(facets, req);

        // render result
        DefaultXmlResponseRenderer renderer = DefaultXmlResponseRenderer.Factory.getInstance();
        renderer.init(null);
        SearchServerResponseInfo info = new SearchServerResponseInfo(1, null, sortedFacets, null);
        ClosableIterator<Document, SearchServerResponseInfo> docStream = new ClosableListIterator<>(Arrays.asList(doc),
            info);
        FusionResponse fusionResponse = new FusionResponse();
        StringWriter sw = new StringWriter();
        fusionResponse.setTextWriter(new PrintWriter(sw));
        fusionResponse.setOk();
        renderer.writeResponse(cfg, docStream, req, fusionResponse);
        String xmlDocStr = sw.toString();
            // System.out.println("DOC " + xmlDocStr);

        // check result
        String expected = "<lst name=\"text18a\">\n" +
            "            <int name=\"a\">1</int>\n" +
            "            <int name=\"b\">2</int>\n" +
            "            <int name=\"c\">3</int>\n" +
            "        </lst>";
        Assert.assertTrue("Didn't find text18a in: " + xmlDocStr, xmlDocStr.contains(expected));
        expected = "<lst name=\"text18b\">\n" +
            "            <int name=\"a\">1</int>\n" +
            "            <int name=\"b\">2</int>\n" +
            "            <int name=\"c\">3</int>\n" +
            "        </lst>";
        Assert.assertTrue("Didn't find text18b in: " + xmlDocStr, xmlDocStr.contains(expected));
    }

    protected DocCount newDocCount(String word, int count)
    {
        DocCount wc = new DocCount();
        wc.setWord(word);
        wc.setCount(count);
        return wc;
    }
}
