package org.outermedia.solrfusion.query;

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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.QueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.*;

public class QueryTest
{

    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void parseEmptyQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query q = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        Assert.assertNull("Expected no query object for empty query string", q);
    }

    @Test
    public void parseWordQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "Schiller";
        Query o = parseQuery(cfg, query);
        String expected = "TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null))";
        Assert.assertEquals("Got different query object than expected", expected, o.toString());

        checkBoost(cfg, "Schiller^0.75", 0.75f);
    }

    protected Query checkBoost(Configuration cfg, String qs, float expectedBoost) throws ParseException
    {
        Query q = parseQuery(cfg, qs);
        Assert.assertNotNull("Expected to get query object for " + qs, q);
        Assert.assertEquals("Found different boost than expected in " + q, expectedBoost, q.getBoostValue());
        return q;
    }

    @Test
    public void parseTermQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        Query o = parseQuery(cfg, "title:Schiller");
        String expected = "TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null))";
        Assert.assertEquals("Got different query object than expected", expected, o.toString());

        String query = "+title:Schiller";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for +...", o instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) o;
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(0).isRequired());

        checkBoost(cfg, "title:Schiller^0.75", 0.75f);

        String qs = "+title:xyzabc^0.75";
        Query q = parseQuery(cfg, qs);
        Assert.assertNotNull("Expected to get query object for " + qs, q);
        q = ((BooleanQuery) q).getClauses().get(0).getQuery();
        Assert.assertEquals("Found different boost than expected in " + q, 0.75f, q.getBoostValue());

        query = "-title:Schiller";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for -...", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertTrue("Expected prohibited flag set for -...", bq.getClauses().get(0).isProhibited());

        // default operator is AND!
        query = "+title:Goethe -title:Schiller title:Hauser";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for -...", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(0).getOccur(),
            bq.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(1).getOccur(),
            bq.getClauses().get(1).isProhibited());
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(2).getOccur(),
            bq.getClauses().get(2).isRequired());
    }

    protected Query parseQuery(Configuration cfg, String query) throws ParseException
    {
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query o = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        Assert.assertNotNull("Expected query object, but couldn't parse query string '" + query + "'", o);
        return o;
    }

    protected void parseQueryException(Configuration cfg, String query, String msg)
    {
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        try
        {
            Query o = p.parse(cfg, boosts, query, Locale.GERMAN, null);
            Assert.fail("Expected exception");
        }
        catch (ParseException e)
        {
            // NOP
        }
    }

    protected void isTermQuery(Query q, String fieldName, String value)
    {
        Assert.assertTrue("Expected TermQuery, but found " + q.getClass().getName(), q instanceof TermQuery);
        TermQuery tq = (TermQuery) q;
        Assert.assertEquals("Found different field name", fieldName, tq.getFusionFieldName());
        Assert.assertEquals("Found different field value", Arrays.asList(value), tq.getFusionFieldValue());
    }

    @Test
    public void parseTermConjunctionQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "title:Schiller title:Müller";
        Query o = parseQuery(cfg, query);
        String expected = "BooleanQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), clauses=[BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null))), BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Müller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null)))])";
        Assert.assertEquals("Got different query object than expected", expected, o.toString());
    }

    @Test
    public void parseBooleanQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "title:Tag AND city:Berlin";
        Query o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for AND", o instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two terms", 2, bq.getClauses().size());
        isTermQuery(bq.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq.getClauses().get(1).getQuery(), "city", "Berlin");
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(1).isRequired());

        query = "title:Tag OR city:Berlin";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for OR", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two terms", 2, bq.getClauses().size());
        isTermQuery(bq.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq.getClauses().get(1).getQuery(), "city", "Berlin");
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(0).isOptional());
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(1).isOptional());

        query = "(title:Tag AND city:Berlin) OR (title:Nacht AND city:Rom)";
        o = parseQuery(cfg, query);
        // System.out.println("Q " + o);
        Assert.assertTrue("Expected bool query for OR", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two bool queries", 2, bq.getClauses().size());
        BooleanQuery bq1 = (BooleanQuery) bq.getClauses().get(0).getQuery();
        BooleanQuery bq2 = (BooleanQuery) bq.getClauses().get(1).getQuery();
        isTermQuery(bq1.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq1.getClauses().get(1).getQuery(), "city", "Berlin");
        isTermQuery(bq2.getClauses().get(0).getQuery(), "title", "Nacht");
        isTermQuery(bq2.getClauses().get(1).getQuery(), "city", "Rom");
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(0).isOptional());
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(1).isOptional());
        Assert.assertTrue("Expected required flag set for +...", bq1.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq1.getClauses().get(1).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq2.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq2.getClauses().get(1).isRequired());

        checkBoost(cfg, "(title:Tag AND city:Berlin)^0.75", 0.75f);
    }

    @Test
    public void testParseDateRangeQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        GregorianCalendar min = new GregorianCalendar(2014, 5, 1);
        GregorianCalendar max = new GregorianCalendar(2014, 5, 26);

        Query q;
        q = parseQuery(cfg, "publicationDate:[01.06.2014 TO 26.06.2014]");
        Assert.assertEquals("Got wrong min value", "20140601", ((DateRangeQuery) q).getMinFusionValue());
        Assert.assertEquals("Got wrong min value", "20140626", ((DateRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "publicationDate:[* TO 26.06.2014]");
        checkNoValue(((DateRangeQuery) q).getMinFusionValue(), "minimum");
        Assert.assertEquals("Got wrong min value", "20140626", ((DateRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "publicationDate:[01.06.2014 TO *]");
        Assert.assertEquals("Got wrong min value", "20140601", ((DateRangeQuery) q).getMinFusionValue());
        checkNoValue(((DateRangeQuery) q).getMaxFusionValue(), "maximum");

        parseQueryException(cfg, "publicationDate:[2014-06-01 TO *]",
            "Expected exception, because of invalid date format");

        checkBoost(cfg, "publicationDate:[01.06.2014 TO *]^0.75", 0.75f);
    }

    protected void checkNoValue(String actual, String ts)
    {
        Assert.assertEquals("Expected * for unset " + ts, "*", actual);
    }

    @Test
    public void testParseIntRangeQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-5";
        String max = "26";

        Query q;
        q = parseQuery(cfg, "numberExample:[-5 TO 26]");
        Assert.assertEquals("Found different minimum", min, ((IntRangeQuery) q).getMinFusionValue());
        Assert.assertEquals("Found different maximum", max, ((IntRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "numberExample:[* TO 26]");
        checkNoValue(((IntRangeQuery) q).getMinFusionValue(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((IntRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "numberExample:[-5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((IntRangeQuery) q).getMinFusionValue());
        checkNoValue(((IntRangeQuery) q).getMaxFusionValue(), "maximum");

        checkBoost(cfg, "numberExample:[-5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseLongRangeQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-5";
        String max = "26";

        Query q;
        q = parseQuery(cfg, "longExample:[-5 TO 26]");
        Assert.assertEquals("Found different minimum", min, ((LongRangeQuery) q).getMinFusionValue());
        Assert.assertEquals("Found different maximum", max, ((LongRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "longExample:[* TO 26]");
        checkNoValue(((LongRangeQuery) q).getMinFusionValue(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((LongRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "longExample:[-5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((LongRangeQuery) q).getMinFusionValue());
        checkNoValue(((LongRangeQuery) q).getMaxFusionValue(), "maximum");

        checkBoost(cfg, "longExample:[-5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseFloatRangeQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-4.5";
        String max = "26.3";

        Query q;
        q = parseQuery(cfg, "floatExample:[-4.5 TO 26.3]");
        Assert.assertEquals("Found different minimum", min, ((FloatRangeQuery) q).getMinFusionValue());
        Assert.assertEquals("Found different maximum", max, ((FloatRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "floatExample:[* TO 26.3]");
        checkNoValue(((FloatRangeQuery) q).getMinFusionValue(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((FloatRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "floatExample:[-4.5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((FloatRangeQuery) q).getMinFusionValue());
        checkNoValue(((FloatRangeQuery) q).getMaxFusionValue(), "maximum");

        checkBoost(cfg, "floatExample:[-4.5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseDoubleRangeQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-4.5";
        String max = "26.3";

        Query q;
        q = parseQuery(cfg, "doubleExample:[-4.5 TO 26.3]");
        Assert.assertEquals("Found different minimum", min, ((DoubleRangeQuery) q).getMinFusionValue());
        Assert.assertEquals("Found different maximum", max, ((DoubleRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "doubleExample:[* TO 26.3]");
        checkNoValue(((DoubleRangeQuery) q).getMinFusionValue(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((DoubleRangeQuery) q).getMaxFusionValue());

        q = parseQuery(cfg, "doubleExample:[-4.5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((DoubleRangeQuery) q).getMinFusionValue());
        checkNoValue(((DoubleRangeQuery) q).getMaxFusionValue(), "maximum");

        checkBoost(cfg, "doubleExample:[-4.5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseFuzzyQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "abcde~2");
        Assert.assertTrue("Expected fuzzy query for ...~...", q instanceof FuzzyQuery);
        FuzzyQuery fq = (FuzzyQuery) q;
        Assert.assertEquals("Found different field name", "title", fq.getTerm().getFusionFieldName());
        Assert.assertEquals("Found different field value", Arrays.asList("abcde"), fq.getTerm().getFusionFieldValue());
        Assert.assertEquals("Found different fuzzy value", Integer.valueOf(2), fq.getMaxEdits());

        fq = (FuzzyQuery) checkBoost(cfg, "abcde~2^0.75", 0.75f);
        Assert.assertEquals("Found different fuzzy value", Integer.valueOf(2), fq.getMaxEdits());
        parseQueryException(cfg, "abcde~5", "Accepted invalid fuzzy slop value");

        fq = (FuzzyQuery) parseQuery(cfg, "abcde~");
        Assert.assertNull("Expected no fuzzy value: " + fq.getMaxEdits(), fq.getMaxEdits());
    }

    @Test
    public void testParseMatchAllDocsQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "*:*");
        Assert.assertTrue("Expected bool query for *:*", q instanceof MatchAllDocsQuery);

        parseQueryException(cfg, "*:*^0.75", "*:* can't have a boost");
    }

    @Test
    public void testParsePrefixQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "abc*");
        Assert.assertTrue("Expected prefix query for ...*", q instanceof PrefixQuery);

        parseQueryException(cfg, "*abc", "Invalid prefix accepted");

        checkBoost(cfg, "abc*^0.75", 0.75f);
    }

    @Test
    public void testParseWildcardQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "ab?c*");
        Assert.assertTrue("Expected prefix query for ...*", q instanceof WildcardQuery);

        parseQueryException(cfg, "?abc", "Invalid prefix accepted");

        checkBoost(cfg, "ab?c*^0.75", 0.75f);
    }

    @Test
    public void testParsePhraseQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        String query = "\"today and tomorrow\"";
        checkPhraseQuery(cfg, query, query.substring(1, query.length() - 1));

        query += "~2";
        PhraseQuery pq = checkPhraseQuery(cfg, query, query.substring(1, query.length() - 3));
        Assert.assertEquals("Expected other fuzzy value", Integer.valueOf(2), pq.getMaxEdits());

        checkBoost(cfg, query + "^0.75", 0.75f);
    }

    protected PhraseQuery checkPhraseQuery(Configuration cfg, String query, String expectedValue) throws ParseException
    {
        Query q;
        q = parseQuery(cfg, query);
        Assert.assertTrue("Expected phrase query for \"...\"", q instanceof PhraseQuery);
        PhraseQuery pq = (PhraseQuery) q;
        Assert.assertEquals("Found different field name", "title", pq.getFusionFieldName());
        Assert.assertEquals("Found different field value", Arrays.asList(expectedValue), pq.getFusionFieldValue());
        return pq;
    }

    @Test
    public void parseQueryWithMetaInfo()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        String query = "{!ex=format_filter}title";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query q = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        // System.out.println("Q " + q);
        MetaInfo mi = q.getMetaInfo();
        Assert.assertNotNull("Expected to find meta info", mi);
        Assert.assertEquals("Found different meta info name", "ex", mi.getName());
        Assert.assertEquals("Found different meta info value", "format_filter", mi.getValue());
    }

    @Test
    public void parseMetaDismax()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        String query = "{!dismax qf=\"title_full_unstemmed^150 title_full^100 title^900 title_alt^200 title_new^100 title_old title_orig^400 series^100 series2 series_orig^100\"}Schiller";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query q = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        // System.out.println("Q " + q);
        checkDismaxQfQuery((TermQuery) q, "Schiller",
            "title_full_unstemmed^150 title_full^100 title^900 title_alt^200 title_new^100 title_old title_orig^400 series^100 series2 series_orig^100");
    }

    protected void checkDismaxQfQuery(TermQuery tq, String queryStr, String subQueryStr)
    {
        Assert.assertEquals("Expected other term", queryStr, tq.getFusionFieldValue().get(0));
        MetaInfo mi = tq.getMetaInfo();
        Assert.assertEquals("Expected other meta key", "dismax", mi.getName());
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("qf", subQueryStr);
        Assert.assertEquals("Expected other params", expected, mi.getFusionParameterMap());
    }

    @Test
    public void parseSubQuery()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        String subQueryStr = "title_full_unstemmed^150 title_full^100 title^900 title_alt^200 title_new^100 title_old title_orig^400 series^100 series2 series_orig^100";
        String query = "((_query_:\"{!dismax qf=\\\"" + subQueryStr + "\\\"}Schiller\"))";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        SubQuery q = (SubQuery) p.parse(cfg, boosts, query, Locale.GERMAN, null);
        // System.out.println("Q " + q);
        checkDismaxQfQuery((TermQuery) q.getQuery(), "Schiller", subQueryStr);
    }

    @Test
    public void parseMultipleSubQueries()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        String goetheSubQuery = "title_full_unstemmed^150 title_full^100 title^900 title_alt^200 title_new^100 title_old title_orig^400 series^100 series2 series_orig^100";
        String raeuberSubQuery = "topic_unstemmed^150 topic^100 topic_id^100 topic_ref^100";
        String query =
            "((_query_:\"{!dismax qf=\\\"" + goetheSubQuery + "\\\"}Goethe\") AND (_query_:\"{!dismax qf=\\\"" +
                raeuberSubQuery + "\\\"}Räuber\"))";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query q = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        // System.out.println("Q " + q);
        BooleanQuery bq = (BooleanQuery) q;
        List<BooleanClause> clauses = bq.getClauses();
        TermQuery query1 = (TermQuery) ((SubQuery) clauses.get(0).getQuery()).getQuery();
        checkDismaxQfQuery(query1, "Goethe", goetheSubQuery);
        TermQuery query2 = (TermQuery) ((SubQuery) clauses.get(1).getQuery()).getQuery();
        checkDismaxQfQuery(query2, "Räuber", raeuberSubQuery);

        // simulate mapping which means for UBL to do (almost) nothing
        query1.getTerm().setWasMapped(true);
        query1.getTerm().setSearchServerFieldValue(query1.getFusionFieldValue());
        query1.setSearchServerFieldName(cfg.getDefaultSearchField());
        MetaInfo mi1 = query1.getMetaInfo();
        mi1.setSearchServerParams(mi1.getFusionParams());

        query2.getTerm().setWasMapped(true);
        query2.getTerm().setSearchServerFieldValue(query2.getFusionFieldValue());
        query2.setSearchServerFieldName(cfg.getDefaultSearchField());
        MetaInfo mi2 = query2.getMetaInfo();
        mi2.setSearchServerParams(mi2.getFusionParams());

        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        String qs = buildQueryString(qb, q, cfg, cfg.getSearchServerConfigByName("UBL"), Locale.GERMAN,
            Sets.newHashSet("allfields"), QueryTarget.ALL);
        // System.out.println("QUERY: "+qs);
        Assert.assertEquals("",
            "(+_query_:\"{!dismax qf=\\\"" + goetheSubQuery + "\\\"}Goethe\" AND +_query_:\"{!dismax qf=\\\"" +
                raeuberSubQuery + "\\\"}Räuber\")", qs);
    }

    protected String buildQueryString(QueryBuilderIfc qb, Query bq, Configuration cfg, SearchServerConfig searchServerConfig,
        Locale locale, Set<String> defaultSearchServerFields, QueryTarget target)
    {
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN, defaultSearchServerFields, target);
        qs = qb.getStaticallyAddedQueries(cfg,searchServerConfig,Locale.GERMAN,target, qs);
        return qs;
    }
}
