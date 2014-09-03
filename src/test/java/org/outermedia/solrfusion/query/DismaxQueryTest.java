package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.ParseException;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.*;

public class DismaxQueryTest
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
        DisMaxQueryParser p = DisMaxQueryParser.Factory.getInstance();
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

        Query o = parseQuery(cfg, "Schiller");
        String expected = "TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null))";
        Assert.assertEquals("Got different query object than expected", expected, o.toString());

        String query = "+Schiller";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for +...", o instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) o;
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(0).isRequired());

        checkBoost(cfg, "Schiller^0.75", 0.75f);

        String qs = "+xyzabc^0.75";
        Query q = parseQuery(cfg, qs);
        Assert.assertNotNull("Expected to get query object for " + qs, q);
        q = ((BooleanQuery) q).getClauses().get(0).getQuery();
        Assert.assertEquals("Found different boost than expected in " + q, 0.75f, q.getBoostValue());

        query = "-Schiller";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for -...", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertTrue("Expected prohibited flag set for -...", bq.getClauses().get(0).isProhibited());

        query = "+Goethe -Schiller Hauser";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for -...", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(0).getOccur(),
            bq.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(1).getOccur(),
            bq.getClauses().get(1).isProhibited());
        Assert.assertTrue("Expected prohibited flag set for -...: " + bq.getClauses().get(2).getOccur(),
            bq.getClauses().get(2).isOptional());
    }

    protected Query parseQuery(Configuration cfg, String query) throws ParseException
    {
        DisMaxQueryParser p = DisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query o = p.parse(cfg, boosts, query, Locale.GERMAN, null);
        Assert.assertNotNull("Expected query object, but couldn't parse query string '" + query + "'", o);
        return o;
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

        String query = "Schiller Müller";
        Query o = parseQuery(cfg, query);
        String expected = "BooleanQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), clauses=[BooleanClause(occur=OCCUR_SHOULD, query=TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null))), BooleanClause(occur=OCCUR_SHOULD, query=TermQuery(super=Query(boostValue=null, addInside=null, metaInfo=null), term=Term(fusionFieldName=title, fusionFieldValue=[Müller], fusionField=FusionField(fieldName=title, type=text, format=null, multiValue=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, processed=false, newQueries=null)))])";
        Assert.assertEquals("Got different query object than expected", expected, o.toString());
    }

    @Test
    public void parseBooleanQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "+Tag +Berlin";
        Query o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query", o instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two terms", 2, bq.getClauses().size());
        isTermQuery(bq.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq.getClauses().get(1).getQuery(), "title", "Berlin");
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(1).isRequired());

        query = "Tag Berlin";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two terms", 2, bq.getClauses().size());
        isTermQuery(bq.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq.getClauses().get(1).getQuery(), "title", "Berlin");
        System.out.println("BC1 " + bq.getClauses().get(0));
        Assert.assertTrue("Expected optional flag set", bq.getClauses().get(0).isOptional());
        Assert.assertTrue("Expected optional flag set", bq.getClauses().get(1).isOptional());

        query = "-Tag -Berlin";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two terms", 2, bq.getClauses().size());
        isTermQuery(bq.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq.getClauses().get(1).getQuery(), "title", "Berlin");
        Assert.assertTrue("Expected prohibited flag set for -...", bq.getClauses().get(0).isProhibited());
        Assert.assertTrue("Expected prohibited flag set for -...", bq.getClauses().get(1).isProhibited());

        query = "(+Tag +Berlin) (+Nacht +Rom)";
        o = parseQuery(cfg, query);
        // System.out.println("Q " + o);
        Assert.assertTrue("Expected bool query for OR", o instanceof BooleanQuery);
        bq = (BooleanQuery) o;
        Assert.assertEquals("Expected only two bool queries", 2, bq.getClauses().size());
        BooleanQuery bq1 = (BooleanQuery) bq.getClauses().get(0).getQuery();
        BooleanQuery bq2 = (BooleanQuery) bq.getClauses().get(1).getQuery();
        isTermQuery(bq1.getClauses().get(0).getQuery(), "title", "Tag");
        isTermQuery(bq1.getClauses().get(1).getQuery(), "title", "Berlin");
        isTermQuery(bq2.getClauses().get(0).getQuery(), "title", "Nacht");
        isTermQuery(bq2.getClauses().get(1).getQuery(), "title", "Rom");
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(0).isOptional());
        Assert.assertTrue("Expected optional flag set for +...", bq.getClauses().get(1).isOptional());
        Assert.assertTrue("Expected required flag set for +...", bq1.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq1.getClauses().get(1).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq2.getClauses().get(0).isRequired());
        Assert.assertTrue("Expected required flag set for +...", bq2.getClauses().get(1).isRequired());

        checkBoost(cfg, "(+Tag +Berlin)^0.75", 0.75f);
    }

    @Test
    public void testParsePhraseQuery()
        throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        Query q;
        String query = "\"today and tomorrow\"";
        checkPhraseQuery(cfg, query, query.substring(1, query.length() - 1));

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
    public void testColon()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, ParseException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        String query = "Karl May : Traum eines Lebens \\- Leben eines Träumers.";
        Query q = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for OR", q instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) q;
        Assert.assertEquals("Expected 10 bool clauses", 10, bq.getClauses().size());
        String expected[] = {"title", "Karl", "title", "May", "title", ":", "title", "Traum", "title", "eines", "title", "Lebens", "title", "-", "title", "Leben", "title", "eines", "title", "Träumers."};
        List<BooleanClause> clauses = bq.getClauses();
        for (int i = 0; i < expected.length; i += 2)
        {
            isTermQuery(clauses.get(i/2).getQuery(), expected[i], expected[i + 1]);
            Assert.assertTrue("Expected optional term query", clauses.get(i/2).isOptional());
        }
    }
}
