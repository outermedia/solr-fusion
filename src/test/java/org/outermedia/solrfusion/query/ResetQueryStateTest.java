package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.mapper.ResetQueryState;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by ballmann on 7/2/14.
 */
public class ResetQueryStateTest
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
        ResetQueryState qb = new ResetQueryState();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        FuzzyQuery fq = new FuzzyQuery(term, null);
        qb.reset(fq);
        Assert.assertFalse("Term not cleaned", term.isWasMapped());
    }

    @Test
    public void testPhraseQuery()
    {
        ResetQueryState qb = new ResetQueryState();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        qb.reset(pq);
        Assert.assertFalse("Term not cleaned", term.isWasMapped());
    }

    @Test
    public void testPrefixQuery()
    {
        ResetQueryState qb = new ResetQueryState();
        Term term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        PrefixQuery pq = new PrefixQuery(term);
        qb.reset(pq);
        Assert.assertFalse("Term not cleaned", term.isWasMapped());
    }

    @Test
    public void testWildcardQuery()
    {
        ResetQueryState qb = new ResetQueryState();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        qb.reset(pq);
        Assert.assertFalse("Term not cleaned", term.isWasMapped());
    }

    @Test
    public void testRangeQueries()
    {
        NumericRangeQuery<?> queryObjects[] = {
            NumericRangeQuery.newDateRange("title", new GregorianCalendar(2014, 5, 26),
                new GregorianCalendar(2014, 6, 1), true, true),
            NumericRangeQuery.newDoubleRange("title", 2.5, 4.5, true, true),
            NumericRangeQuery.newFloatRange("title", 2.5f, 4.5f, true, true),
            NumericRangeQuery.newIntRange("title", 2, 4, true, true),
            NumericRangeQuery.newLongRange("title", 2L, 4L, true, true)
        };
        int at = 0;
        for (NumericRangeQuery<?> nq : queryObjects)
        {
            ResetQueryState qb = new ResetQueryState();
            Term minMax = nq.getTerm();
            minMax.setWasMapped(true);
            minMax.setSearchServerFieldName("title");
            minMax.setSearchServerFieldValue(minMax.getFusionFieldValue());
            qb.reset(nq);
            Assert.assertFalse("minMax term not cleaned", minMax.isWasMapped());
        }
    }

    @Test
    public void testBoolQuery()
    {
        List<Term> termList = new ArrayList<>();

        // empty case
        ResetQueryState qb = new ResetQueryState();
        BooleanQuery bq = new BooleanQuery();
        qb.reset(bq);

        // one MUST clause
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // one SHOULD clause
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // one MUST NOT clause
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // several MUST clauses
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", true, termList));
        bq.add(createMustBooleanClause("def", true, termList));
        bq.add(createMustNotBooleanClause("ghi", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // several SHOULD clauses
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", true, termList));
        bq.add(createShouldBooleanClause("def", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // several MUST NOT clauses
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", true, termList));
        bq.add(createMustNotBooleanClause("def", true, termList));
        bq.add(createMustBooleanClause("ghi", true, termList));
        bq.add(createShouldBooleanClause("jkl", true, termList));
        qb.reset(bq);
        checkReset(termList);

        // nested bool queries
        qb = new ResetQueryState();
        termList.clear();
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true, termList));
        bq1.add(createMustBooleanClause("def", true, termList));
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", true, termList));
        bq2.add(createMustBooleanClause("jkl", true, termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qb.reset(bq);
        checkReset(termList);

        // several deleted clauses
        qb = new ResetQueryState();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", false, termList)); // del
        bq.add(createMustBooleanClause("def", true, termList));
        bq.add(createShouldBooleanClause("ghi", false, termList)); // del
        bq.add(createShouldBooleanClause("jkl", true, termList));
        bq.add(createMustBooleanClause("mno", false, termList)); // del
        qb.reset(bq);
        checkReset(termList);

        // delete whole bool query
        qb = new ResetQueryState();
        termList.clear();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true, termList));
        bq1.add(createMustBooleanClause("def", true, termList));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false, termList));
        bq2.add(createMustBooleanClause("jkl", false, termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qb.reset(bq);
        checkReset(termList);

        // delete all bool queries
        qb = new ResetQueryState();
        termList.clear();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", false, termList));
        bq1.add(createMustBooleanClause("def", false, termList));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false, termList));
        bq2.add(createMustBooleanClause("jkl", false, termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qb.reset(bq);
        checkReset(termList);
    }

    protected void checkReset(List<Term> terms)
    {
        for(Term t : terms)
        {
            Assert.assertFalse("Term not cleaned", t.isWasMapped());
        }
    }

    protected BooleanClause createShouldBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustBooleanClause(String s, boolean mapped, List<Term> terms)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST);
    }

    protected BooleanClause createShouldBooleanClause(String s, boolean mapped, List<Term> terms)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustNotBooleanClause(String s, boolean mapped, List<Term> terms)
    {
        Term term = Term.newSearchServerTerm("title", s);
        term.setWasMapped(mapped);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST_NOT);
    }

    @Test
    public void testSubQuery()
    {
        ResetQueryState qb = new ResetQueryState();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        SubQuery sq = new SubQuery(pq);
        MetaInfo mi = new MetaInfo();
        mi.setSearchServerParams(new MetaParams());
        mi.addSearchServerEntry("a","1");
        pq.setMetaInfo(mi);
        qb.reset(sq);
        Assert.assertFalse("Term not cleaned", term.isWasMapped());
        Assert.assertNull("MetaInfo not cleaned", mi.getSearchServerParameterMap());
    }
}
