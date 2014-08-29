package org.outermedia.solrfusion.query;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.DisMaxQueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by ballmann on 7/2/14.
 */
public class DisMaxQueryBuilderTest
{
    Configuration cfg;
    TestHelper helper;
    SearchServerConfig searchServerConfig;
    Locale locale = Locale.GERMAN;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
    }

    @Test
    public void testFuzzyQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        FuzzyQuery fq = new FuzzyQuery(term, null);
        String qs = qb.buildQueryString(fq, cfg, searchServerConfig, locale, null);
        Assert.assertEquals("Got different fuzzy query than expected", "", qs);
    }

    @Test
    public void testMatchAllDocsQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        MatchAllDocsQuery fq = new MatchAllDocsQuery();
        String qs = qb.buildQueryString(fq, cfg, searchServerConfig, locale, null);
        Assert.assertEquals("Got different *:* query than expected", "", qs);
    }

    @Test
    public void testPhraseQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        String qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"", qs);

        qb = DisMaxQueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        pq = new PhraseQuery(term);
        pq.setBoost(3.5f);
        qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"^3.5", qs);
    }

    @Test
    public void testPrefixQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        PrefixQuery pq = new PrefixQuery(term);
        String qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, null);
        Assert.assertEquals("Got different prefix query than expected", "", qs);
    }

    @Test
    public void testWildcardQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        String qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, null);
        Assert.assertEquals("Got different wildcard query than expected", "", qs);
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
        for (NumericRangeQuery<?> nq : queryObjects)
        {
            QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
            Term minMax = nq.getTerm();
            minMax.setWasMapped(true);
            minMax.setSearchServerFieldName("title");
            minMax.setSearchServerFieldValue(minMax.getFusionFieldValue());
            String qs = qb.buildQueryString(nq, cfg, searchServerConfig, locale, null);
            Assert.assertEquals("Got different min value than expected", "", qs);
        }
    }

    @Test
    public void testBoolQuery()
    {
        // empty case
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        BooleanQuery bq = new BooleanQuery();
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, null);
        Assert.assertEquals("Got different bool query than expected", "", qs);

        // one MUST clause
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "+abc", qs);

        // one SHOULD clause
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "abc", qs);

        // one MUST NOT clause
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "-abc", qs);

        // several MUST clauses
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        bq.add(createMustBooleanClause("def", true));
        bq.add(createMustNotBooleanClause("ghi", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "+abc +def -ghi", qs);

        // several SHOULD clauses
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        bq.add(createShouldBooleanClause("def", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "abc def", qs);

        // several MUST NOT clauses
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        bq.add(createMustNotBooleanClause("def", true));
        bq.add(createMustBooleanClause("ghi", true));
        bq.add(createShouldBooleanClause("jkl", true));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "-abc -def +ghi jkl", qs);

        // nested bool queries
        qb = DisMaxQueryBuilder.Factory.getInstance();
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", true));
        bq2.add(createMustBooleanClause("jkl", true));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "+abc +def +ghi +jkl", qs);

        // several deleted clauses
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", false)); // del
        bq.add(createMustBooleanClause("def", true));
        bq.add(createShouldBooleanClause("ghi", false)); // del
        bq.add(createShouldBooleanClause("jkl", true));
        bq.add(createMustBooleanClause("mno", false)); // del
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "+def jkl", qs);

        // delete whole bool query
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different bool query than expected", "+abc +def", qs);

        // delete all bool queries
        qb = DisMaxQueryBuilder.Factory.getInstance();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", false));
        bq1.add(createMustBooleanClause("def", false));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = qb.buildQueryString(bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
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

    @Test
    public void testSubQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        SubQuery sq = new SubQuery(pq);
        String qs = qb.buildQueryString(sq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different sub query than expected", "", qs);
    }

    @Test
    public void testMetaInfo()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        MetaInfo mi = new MetaInfo();
        mi.setSearchServerParams(new MetaParams());
        mi.addSearchServerEntry("a","1");
        pq.setMetaInfo(mi);
        String qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}\"abc\"", qs);

        // name set in MetaInfo
        qb = DisMaxQueryBuilder.Factory.getInstance();
        mi.setName("dismax");
        qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "{!dismax a=1}\"abc\"", qs);

        // name not set, but value
        qb = DisMaxQueryBuilder.Factory.getInstance();
        mi.setName(null);
        mi.setValue("abc");
        qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}\"abc\"", qs);

        // name, value and params set
        mi.setName("dismax");
        qb = DisMaxQueryBuilder.Factory.getInstance();
        qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "{!dismax=abc a=1}\"abc\"", qs);

        // several params set
        qb = DisMaxQueryBuilder.Factory.getInstance();
        mi.addSearchServerEntry("b","2");
        qs = qb.buildQueryString(pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"));
        Assert.assertEquals("Got different phrase query than expected", "{!dismax=abc a=1 b=2}\"abc\"", qs);
    }
}
