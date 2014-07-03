package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.mapper.QueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;

/**
 * Created by ballmann on 7/2/14.
 */
public class QueryBuilderTest
{
    Configuration cfg;
    TestHelper helper;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
    }

    @Test
    public void testFuzzyQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        FuzzyQuery fq = new FuzzyQuery(term, null);
        String qs = qb.buildQueryString(fq, cfg);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc~", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        fq = new FuzzyQuery(term, 3);
        qs = qb.buildQueryString(fq, cfg);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc~", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        fq = new FuzzyQuery(term, 3);
        fq.setBoost(3.5f);
        qs = qb.buildQueryString(fq, cfg);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc^3.5~", qs);
    }

    @Test
    public void testMatchAllDocsQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        MatchAllDocsQuery fq = new MatchAllDocsQuery();
        String qs = qb.buildQueryString(fq, cfg);
        Assert.assertEquals("Got different *:* query than expected", "*:*", qs);
    }

    @Test
    public void testPhraseQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        String qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:\"abc\"", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        pq = new PhraseQuery(term);
        pq.setBoost(3.5f);
        qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:\"abc\"^3.5", qs);
    }

    @Test
    public void testPrefixQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        PrefixQuery pq = new PrefixQuery(term);
        String qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:abc*", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        pq = new PrefixQuery(term);
        pq.setBoost(3.5f);
        qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:abc*^3.5", qs);
    }

    @Test
    public void testWildcardQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        String qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:abc?", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        pq = new WildcardQuery(term);
        pq.setBoost(3.5f);
        qs = qb.buildQueryString(pq, cfg);
        Assert.assertEquals("Got different phrase query than expected", "title:abc?^3.5", qs);
    }

    @Test
    public void testRangeQueries()
    {
        testRangeQueries(null);
        testRangeQueries(3.6f);
    }

    protected void testRangeQueries(Float boost)
    {
        NumericRangeQuery<?> queryObjects[] = {
            NumericRangeQuery.newDateRange("title", new GregorianCalendar(2014, 5, 26),
                new GregorianCalendar(2014, 6, 1), true, true),
            NumericRangeQuery.newDoubleRange("title", 2.5, 4.5, true, true),
            NumericRangeQuery.newFloatRange("title", 2.5f, 4.5f, true, true),
            NumericRangeQuery.newIntRange("title", 2, 4, true, true),
            NumericRangeQuery.newLongRange("title", 2L, 4L, true, true)
        };
        String expectedMinMax[] = {
            "title:[20140626 TO 20140701]", "title:[2.5 TO 4.5]", "title:[2.5 TO 4.5]", "title:[2 TO 4]", "title:[2 TO 4]"
        };
        int at = 0;
        for (NumericRangeQuery<?> nq : queryObjects)
        {
            nq.setBoostValue(boost);
            QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
            Term min = nq.getMin();
            min.setWasMapped(true);
            min.setSearchServerFieldName("title");
            min.setSearchServerFieldValue(min.getFusionFieldValue());
            Term max = nq.getMax();
            max.setWasMapped(true);
            max.setSearchServerFieldName("title");
            max.setSearchServerFieldValue(max.getFusionFieldValue());
            String qs = qb.buildQueryString(nq, cfg);
            String expected = expectedMinMax[at++];
            if (boost != null)
            {
                expected += "^" + boost;
            }
            Assert.assertEquals("Got different min value than expected", expected, qs);
        }
    }

    @Test
    public void testBoolQuery()
    {
        // empty case
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        BooleanQuery bq = new BooleanQuery();
        String qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "", qs);

        // one MUST clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(+title:abc)", qs);

        // one SHOULD clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(title:abc)", qs);

        // one MUST NOT clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(-title:abc)", qs);

        // several MUST clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        bq.add(createMustBooleanClause("def", true));
        bq.add(createMustNotBooleanClause("ghi", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(+title:abc AND +title:def AND -title:ghi)", qs);

        // several SHOULD clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        bq.add(createShouldBooleanClause("def", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(title:abc OR title:def)", qs);

        // several MUST NOT clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        bq.add(createMustNotBooleanClause("def", true));
        bq.add(createMustBooleanClause("ghi", true));
        bq.add(createShouldBooleanClause("jkl", true));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(-title:abc AND -title:def AND +title:ghi OR title:jkl)", qs);

        // nested bool queries
        qb = QueryBuilder.Factory.getInstance();
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", true));
        bq2.add(createMustBooleanClause("jkl", true));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "((+title:abc AND +title:def) OR (+title:ghi AND +title:jkl))", qs);

        // several deleted clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", false)); // del
        bq.add(createMustBooleanClause("def", true));
        bq.add(createShouldBooleanClause("ghi", false)); // del
        bq.add(createShouldBooleanClause("jkl", true));
        bq.add(createMustBooleanClause("mno", false)); // del
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "(+title:def OR title:jkl)", qs);

        // delete whole bool query
        qb = QueryBuilder.Factory.getInstance();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "((+title:abc AND +title:def))", qs);

        // delete all bool queries
        qb = QueryBuilder.Factory.getInstance();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", false));
        bq1.add(createMustBooleanClause("def", false));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Got different bool query than expected", "", qs);
    }

    protected BooleanClause createShouldBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustBooleanClause(String s, boolean mapped)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST);
    }

    protected BooleanClause createShouldBooleanClause(String s, boolean mapped)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustNotBooleanClause(String s, boolean mapped)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST_NOT);
    }
}
