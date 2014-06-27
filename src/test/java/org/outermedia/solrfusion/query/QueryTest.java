package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.BooleanQuery;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        String expected = "TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))";
        Assert.assertEquals("Got different query object than expected",
                expected, o.toString());
    }

    @Test
    public void parseTermQuery() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        Query o = parseQuery(cfg, "title:Schiller");
        String expected = "TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))";
        Assert.assertEquals("Got different query object than expected",
                expected, o.toString());

        String query = "+title:Schiller";
        o = parseQuery(cfg, query);
        Assert.assertTrue("Expected bool query for +...", o instanceof BooleanQuery);
        BooleanQuery bq = (BooleanQuery) o;
        Assert.assertTrue("Expected required flag set for +...", bq.getClauses().get(0).isRequired());

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
        Query o = p.parse(cfg, boosts, query);
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
    public void parseTermConjunctionQuery() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "title:Schiller title:Müller";
        Query o = parseQuery(cfg, query);
        String expected = "BooleanQuery(super=Query(), clauses=[BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null))), BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=[Müller], fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newQueryTerms=null, newResponseValues=null)))])";
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
    }

    // TODO DateRangeQuery
    // TODO IntRangeQuery
    // TODO LongRangeQuery
    // TODO FloatRangeQuery
    // TODO DoubleRangeQuery
    // TODO FuzzyQuery
    // TODO MatchAllDocsQuery
    // TODO MultiPhraseQuery
    // TODO PhraseQuery
    // TODO PrefixQuery
    // TODO WildcardQuery
}
