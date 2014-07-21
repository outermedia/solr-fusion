package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class QueryMapperTest
{
    protected TestHelper helper;
    protected Configuration cfg;
    protected ScriptEnv env;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        env = new ScriptEnv();
    }

    @Test
    public void testSimpleQueryMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), q, env);

        String expected = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, processed=true, newQueryTerms=null)";
        Assert.assertEquals("Got different mapping than expected", expected, q.getTerm().toString());

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(q, cfg);
        Assert.assertEquals("Found wrong search server term query mapping", "Autor:Schiller", s);
    }

    @Test
    public void testQueryConjunctionMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, processed=true, newQueryTerms=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=[Ein_langer_Weg], fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=[Ein_langer_Weg], removed=false, wasMapped=true, processed=true, newQueryTerms=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Found wrong search server bool query mapping", "(+Autor:Schiller AND +Titel:Ein_langer_Weg)",
                s.trim());
    }

    @Test
    public void testQueryDisjunctionMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST_NOT));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST_NOT));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, processed=true, newQueryTerms=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=[Ein_langer_Weg], fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=[Ein_langer_Weg], removed=false, wasMapped=true, processed=true, newQueryTerms=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Found wrong search server bool query mapping", "(-Autor:Schiller AND -Titel:Ein_langer_Weg)",
                s.trim());

        ResetQueryState resetter = new ResetQueryState();
        resetter.reset(bq);
        Assert.assertNull("Expected empty fusion field name after clear", q.getSearchServerFieldName());
        Assert.assertNull("Expected empty fusion field value after clear", q.getSearchServerFieldValue());
        Assert.assertFalse("Expected false for removed after clear", q.getTerm().isRemoved());
        Assert.assertFalse("Expected false for wasMapped after clear", q.getTerm().isWasMapped());
        Assert.assertNull("Expected empty fusion field name after clear", q2.getSearchServerFieldName());
        Assert.assertNull("Expected empty fusion field value after clear", q2.getSearchServerFieldValue());
        Assert.assertFalse("Expected false for removed after clear", q2.getTerm().isRemoved());
        Assert.assertFalse("Expected false for wasMapped after clear", q2.getTerm().isWasMapped());
    }

    @Test
    public void testRegExpr() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("valueFrom7", "Schiller"));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), q, env);
        Term term = q.getTerm();
        String searchServerFieldName = term.getSearchServerFieldName();
        Assert.assertEquals("RegExp mapping returned different search server field than expected", "val7Start",
                searchServerFieldName);
        List<String> searchServerFieldValue = term.getSearchServerFieldValue();
        Assert.assertEquals("RegExp mapping returned different search server field value than expected", Arrays.asList(
                "Schiller"), searchServerFieldValue);
    }

    @Test
    public void testWildcardHandling()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
        InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        MatchAllDocsQuery wq = new MatchAllDocsQuery();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), wq, env);

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(wq, cfg);
        Assert.assertEquals("Expected match all docs query", "*:*", s);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void testFuzzyQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("title", "abc");
        FuzzyQuery fq = new FuzzyQuery(term, null);
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), fq, env);
        Assert.assertTrue("Term not mapped", term.isWasMapped());
    }

    @Test
    public void testPhraseQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("title", "abc");
        PhraseQuery pq = new PhraseQuery(term);
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), pq, env);
        Assert.assertTrue("Term not mapped", term.isWasMapped());
    }

    @Test
    public void testPrefixQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("title", "abc*");
        PrefixQuery pq = new PrefixQuery(term);
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), pq, env);
        Assert.assertTrue("Term not mapped", term.isWasMapped());
    }

    @Test
    public void testWildcardQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("title", "abc?");
        WildcardQuery pq = new WildcardQuery(term);
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), pq, env);
        Assert.assertTrue("Term not mapped", term.isWasMapped());
    }

    @Test
    public void testRangeQueries() throws InvocationTargetException, IllegalAccessException
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
            QueryMapperIfc qm = cfg.getQueryMapper();
            Term min = nq.getMin();
            min.newFusionTerm("title");
            min.setFusionFieldValue(min.getFusionFieldValue());
            Term max = nq.getMax();
            max.newFusionTerm("title");
            max.setFusionFieldValue(max.getFusionFieldValue());
            qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), nq, env);
            Assert.assertTrue("Term not mapped", min.isWasMapped());
            Assert.assertTrue("Term not mapped", max.isWasMapped());
        }
    }

    @Test
    public void testBoolQuery() throws InvocationTargetException, IllegalAccessException
    {
        List<Term> termList = new ArrayList<>();

        // empty case
        QueryMapperIfc qm = cfg.getQueryMapper();
        BooleanQuery bq = new BooleanQuery();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        // one MUST clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // one SHOULD clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // one MUST NOT clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // several MUST clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", termList));
        bq.add(createMustBooleanClause("def", termList));
        bq.add(createMustNotBooleanClause("ghi", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // several SHOULD clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", termList));
        bq.add(createShouldBooleanClause("def", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // several MUST NOT clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList));
        bq.add(createMustNotBooleanClause("def", termList));
        bq.add(createMustBooleanClause("ghi", termList));
        bq.add(createShouldBooleanClause("jkl", termList));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // nested bool queries
        qm = cfg.getQueryMapper();
        termList.clear();
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", termList));
        bq1.add(createMustBooleanClause("def", termList));
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", termList));
        bq2.add(createMustBooleanClause("jkl", termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // several deleted clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList)); // del
        bq.add(createMustBooleanClause("def", termList));
        bq.add(createShouldBooleanClause("ghi", termList)); // del
        bq.add(createShouldBooleanClause("jkl", termList));
        bq.add(createMustBooleanClause("mno", termList)); // del
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // delete whole bool query
        qm = cfg.getQueryMapper();
        termList.clear();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", termList));
        bq1.add(createMustBooleanClause("def", termList));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", termList));
        bq2.add(createMustBooleanClause("jkl", termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);

        // delete all bool queries
        qm = cfg.getQueryMapper();
        termList.clear();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", termList));
        bq1.add(createMustBooleanClause("def", termList));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", termList));
        bq2.add(createMustBooleanClause("jkl", termList));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);
        checkMapped(termList);
    }

    protected void checkMapped(List<Term> terms)
    {
        for(Term t : terms)
        {
            Assert.assertTrue("Term not mapped", t.isWasMapped());
        }
    }

    protected BooleanClause createShouldBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("title", s);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST);
    }

    protected BooleanClause createShouldBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("title", s);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustNotBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("title", s);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST_NOT);
    }

}
