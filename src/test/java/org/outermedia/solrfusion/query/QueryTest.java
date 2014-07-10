package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.mapper.Term;
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
    public void parseWordQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "Schiller";
        Query o = parseQuery(cfg, query);
        String expected = "TermQuery(super=Query(boostValue=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))";
        Assert.assertEquals("Got different query object than expected",
            expected, o.toString());

        checkBoost(cfg, "Schiller^0.75", 0.75f);
    }

    protected Query checkBoost(Configuration cfg, String qs, float expectedBoost)
    {
        Query q = parseQuery(cfg, qs);
        Assert.assertNotNull("Expected to get query object for " + qs, q);
        Assert.assertEquals("Found different boost than expected in " + q, expectedBoost, q.getBoostValue());
        return q;
    }

    @Test
    public void parseTermQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        Query o = parseQuery(cfg, "title:Schiller");
        String expected = "TermQuery(super=Query(boostValue=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))";
        Assert.assertEquals("Got different query object than expected",
            expected, o.toString());

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

    protected Query parseQuery(Configuration cfg, String query)
    {
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query o = p.parse(cfg, boosts, query, Locale.GERMAN);
        Assert.assertNotNull("Expected query object, but couldn't parse query string '" + query + "'", o);
        return o;
    }

    protected void parseQueryException(Configuration cfg, String query, String msg)
    {
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query o = p.parse(cfg, boosts, query, Locale.GERMAN);
        Assert.assertNull(msg, o);
    }

    protected void isTermQuery(Query q, String fieldName, String value)
    {
        Assert.assertTrue("Expected TermQuery, but found " + q.getClass().getName(), q instanceof TermQuery);
        TermQuery tq = (TermQuery) q;
        Assert.assertEquals("Found different field name", fieldName, tq.getFusionFieldName());
        Assert.assertEquals("Found different field value", Arrays.asList(value), tq.getFusionFieldValue());
    }

    @Test
    public void parseTermConjunctionQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "title:Schiller title:Müller";
        Query o = parseQuery(cfg, query);
        String expected = "BooleanQuery(super=Query(boostValue=null), clauses=[BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(boostValue=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))), BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(boostValue=null), term=Term(fusionFieldName=title, fusionFieldValue=[Müller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null)))])";
        Assert.assertEquals("Got different query object than expected",
            expected, o.toString());
    }

    @Test
    public void parseBooleanQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

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
    public void testParseDateRangeQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        GregorianCalendar min = new GregorianCalendar(2014, 5, 1);
        GregorianCalendar max = new GregorianCalendar(2014, 5, 26);

        Query q;
        q = parseQuery(cfg, "publicationDate:[01.06.2014 TO 26.06.2014]");
        Assert.assertEquals("Got wrong min value", "20140601",
            ((DateRangeQuery) q).getMin().getFusionFieldValue().get(0));
        Assert.assertEquals("Got wrong min value", "20140626",
            ((DateRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "publicationDate:[* TO 26.06.2014]");
        checkNoValue(((DateRangeQuery) q).getMin(), "minimum");
        Assert.assertEquals("Got wrong min value", "20140626",
            ((DateRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "publicationDate:[01.06.2014 TO *]");
        Assert.assertEquals("Got wrong min value", "20140601",
            ((DateRangeQuery) q).getMin().getFusionFieldValue().get(0));
        checkNoValue(((DateRangeQuery) q).getMax(), "maximum");

        parseQueryException(cfg, "publicationDate:[2014-06-01 TO *]",
            "Expected exception, because of invalid date format");

        checkBoost(cfg, "publicationDate:[01.06.2014 TO *]^0.75", 0.75f);
    }

    protected void checkNoValue(Term t, String ts)
    {
        Assert.assertEquals("Expected * for unset " + ts, "*", t.getFusionFieldValue().get(0));
    }

    @Test
    public void testParseIntRangeQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-5";
        String max = "26";

        Query q;
        q = parseQuery(cfg, "numberExample:[-5 TO 26]");
        Assert.assertEquals("Found different minimum", min, ((IntRangeQuery) q).getMin().getFusionFieldValue().get(0));
        Assert.assertEquals("Found different maximum", max, ((IntRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "numberExample:[* TO 26]");
        checkNoValue(((IntRangeQuery) q).getMin(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((IntRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "numberExample:[-5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((IntRangeQuery) q).getMin().getFusionFieldValue().get(0));
        checkNoValue(((IntRangeQuery) q).getMax(), "maximum");

        checkBoost(cfg, "numberExample:[-5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseLongRangeQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-5";
        String max = "26";

        Query q;
        q = parseQuery(cfg, "longExample:[-5 TO 26]");
        Assert.assertEquals("Found different minimum", min, ((LongRangeQuery) q).getMin().getFusionFieldValue().get(0));
        Assert.assertEquals("Found different maximum", max, ((LongRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "longExample:[* TO 26]");
        checkNoValue(((LongRangeQuery) q).getMin(), "minimum");
        Assert.assertEquals("Found different maximum", max, ((LongRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "longExample:[-5 TO *]");
        Assert.assertEquals("Found different minimum", min, ((LongRangeQuery) q).getMin().getFusionFieldValue().get(0));
        checkNoValue(((LongRangeQuery) q).getMax(), "maximum");

        checkBoost(cfg, "longExample:[-5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseFloatRangeQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-4.5";
        String max = "26.3";

        Query q;
        q = parseQuery(cfg, "floatExample:[-4.5 TO 26.3]");
        Assert.assertEquals("Found different minimum", min,
            ((FloatRangeQuery) q).getMin().getFusionFieldValue().get(0));
        Assert.assertEquals("Found different maximum", max,
            ((FloatRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "floatExample:[* TO 26.3]");
        checkNoValue(((FloatRangeQuery) q).getMin(), "minimum");
        Assert.assertEquals("Found different maximum", max,
            ((FloatRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "floatExample:[-4.5 TO *]");
        Assert.assertEquals("Found different minimum", min,
            ((FloatRangeQuery) q).getMin().getFusionFieldValue().get(0));
        checkNoValue(((FloatRangeQuery) q).getMax(), "maximum");

        checkBoost(cfg, "floatExample:[-4.5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseDoubleRangeQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String min = "-4.5";
        String max = "26.3";

        Query q;
        q = parseQuery(cfg, "doubleExample:[-4.5 TO 26.3]");
        Assert.assertEquals("Found different minimum", min,
            ((DoubleRangeQuery) q).getMin().getFusionFieldValue().get(0));
        Assert.assertEquals("Found different maximum", max,
            ((DoubleRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "doubleExample:[* TO 26.3]");
        checkNoValue(((DoubleRangeQuery) q).getMin(), "minimum");
        Assert.assertEquals("Found different maximum", max,
            ((DoubleRangeQuery) q).getMax().getFusionFieldValue().get(0));

        q = parseQuery(cfg, "doubleExample:[-4.5 TO *]");
        Assert.assertEquals("Found different minimum", min,
            ((DoubleRangeQuery) q).getMin().getFusionFieldValue().get(0));
        checkNoValue(((DoubleRangeQuery) q).getMax(), "maximum");

        checkBoost(cfg, "doubleExample:[-4.5 TO *]^0.75", 0.75f);
    }

    @Test
    public void testParseFuzzyQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
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
    public void testParseMatchAllDocsQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "*:*");
        Assert.assertTrue("Expected bool query for *:*", q instanceof MatchAllDocsQuery);

        parseQueryException(cfg, "*:*^0.75", "*:* can't have a boost");
    }

    @Test
    public void testParsePrefixQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "abc*");
        Assert.assertTrue("Expected prefix query for ...*", q instanceof PrefixQuery);

        parseQueryException(cfg, "*abc", "Invalid prefix accepted");

        checkBoost(cfg, "abc*^0.75", 0.75f);
    }

    @Test
    public void testParseWildcardQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        q = parseQuery(cfg, "ab?c*");
        Assert.assertTrue("Expected prefix query for ...*", q instanceof WildcardQuery);

        parseQueryException(cfg, "?abc", "Invalid prefix accepted");

        checkBoost(cfg, "ab?c*^0.75", 0.75f);
    }

    @Test
    public void testParsePhraseQuery() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException
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

    protected PhraseQuery checkPhraseQuery(Configuration cfg, String query, String expectedValue)
    {
        Query q;
        q = parseQuery(cfg, query);
        Assert.assertTrue("Expected phrase query for \"...\"", q instanceof PhraseQuery);
        PhraseQuery pq = (PhraseQuery) q;
        Assert.assertEquals("Found different field name", "title", pq.getFusionFieldName());
        Assert.assertEquals("Found different field value", Arrays.asList(
            expectedValue), pq.getFusionFieldValue());
        return pq;
    }

}
