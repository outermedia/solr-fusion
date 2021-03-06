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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.QueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ballmann on 7/2/14.
 */
public class QueryBuilderTest
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
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        FuzzyQuery fq = new FuzzyQuery(term, null);
        String qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc~", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        fq = new FuzzyQuery(term, 3);
        qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc~", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        fq = new FuzzyQuery(term, 3);
        fq.setBoost(3.5f);
        qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different fuzzy query than expected", "title:abc^3.5~", qs);
    }

    protected String buildQueryString(QueryBuilderIfc qb, Query bq, Configuration cfg,
        SearchServerConfig searchServerConfig, Locale locale, Set<String> defaultSearchServerFields, QueryTarget target)
    {
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN, defaultSearchServerFields, target);
        qs = qb.getStaticallyAddedQueries(cfg, searchServerConfig, Locale.GERMAN, target, qs);
        return qs;
    }

    @Test
    public void testMatchAllDocsQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        MatchAllDocsQuery fq = new MatchAllDocsQuery();
        String qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different *:* query than expected", "*:*", qs);
    }

    @Test
    public void testPhraseQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "title:\"abc\"", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        pq = new PhraseQuery(term);
        pq.setBoost(3.5f);
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "title:\"abc\"^3.5", qs);
    }

    @Test
    public void testPrefixQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        PrefixQuery pq = new PrefixQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different prefix query than expected", "title:abc*", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        pq = new PrefixQuery(term);
        pq.setBoost(3.5f);
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different prefix query than expected", "title:abc*^3.5", qs);
    }

    @Test
    public void testWildcardQuery()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different wildcard query than expected", "title:abc?", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        pq = new WildcardQuery(term);
        pq.setBoost(3.5f);
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different wildcard query than expected", "title:abc?^3.5", qs);
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
            "title:[20140626 TO 20140701]", "title:[2.5 TO 4.5]", "title:[2.5 TO 4.5]", "title:[2 TO 4]",
            "title:[2 TO 4]"
        };
        int at = 0;
        for (NumericRangeQuery<?> nq : queryObjects)
        {
            nq.setBoostValue(boost);
            QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
            Term minMax = nq.getTerm();
            minMax.setWasMapped(true);
            minMax.setSearchServerFieldName("title");
            minMax.setSearchServerFieldValue(minMax.getFusionFieldValue());
            String qs = buildQueryString(qb, nq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
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
        String qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "", qs);

        // one MUST clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "(+title:abc)", qs);

        // one SHOULD clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "(title:abc)", qs);

        // one MUST NOT clause
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "(-title:abc)", qs);

        // several MUST clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        bq.add(createMustBooleanClause("def", true));
        bq.add(createMustNotBooleanClause("ghi", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "(+title:abc AND +title:def AND -title:ghi)", qs);

        // several SHOULD clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        bq.add(createShouldBooleanClause("def", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "(title:abc OR title:def)", qs);

        // several MUST NOT clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        bq.add(createMustNotBooleanClause("def", true));
        bq.add(createMustBooleanClause("ghi", true));
        bq.add(createShouldBooleanClause("jkl", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected",
            "(-title:abc AND -title:def AND +title:ghi OR title:jkl)", qs);

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
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected",
            "((+title:abc AND +title:def) OR (+title:ghi AND +title:jkl))", qs);

        // +(title:abc OR title:def) AND +(title:ABC OR title:DEF)
        qb = QueryBuilder.Factory.getInstance();
        bq1 = new BooleanQuery();
        bq1.add(createShouldBooleanClause("abc", true));
        bq1.add(createShouldBooleanClause("def", true));
        bq2 = new BooleanQuery();
        bq2.add(createShouldBooleanClause("ghi", true));
        bq2.add(createShouldBooleanClause("jkl", true));
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause(bq1));
        bq.add(createMustBooleanClause(bq2));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected",
            "(+(title:abc OR title:def) AND +(title:ghi OR title:jkl))", qs);

        // several deleted clauses
        qb = QueryBuilder.Factory.getInstance();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", false)); // del
        bq.add(createMustBooleanClause("def", true));
        bq.add(createShouldBooleanClause("ghi", false)); // del
        bq.add(createShouldBooleanClause("jkl", true));
        bq.add(createMustBooleanClause("mno", false)); // del
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
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
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
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
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
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

    protected BooleanClause createMustBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST);
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
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        SubQuery sq = new SubQuery(pq);
        String qs = buildQueryString(qb, sq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different sub query than expected", "_query_:\"title:\\\"abc?\\\"\"", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        pq = new PhraseQuery(term);
        pq.setBoost(3.5f);
        sq = new SubQuery(pq);
        qs = buildQueryString(qb, sq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different sub query than expected", "_query_:\"title:\\\"abc?\\\"^3.5\"", qs);
    }

    @Test
    public void testMetaInfo()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        MetaInfo mi = new MetaInfo();
        mi.setSearchServerParams(new MetaParams());
        mi.addSearchServerEntry("a", "1");
        pq.setMetaInfo(mi);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}title:\"abc\"", qs);

        // name not set, but value
        qb = QueryBuilder.Factory.getInstance();
        mi.setName(null);
        mi.setValue("abc");
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}title:\"abc\"", qs);
    }

    @Test
    public void testEscaping()
    {
        QueryBuilderIfc qb = QueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("multipart_link", "|!&urn:nbn:de:bsz:{14}-[qucosa]-(108346)!^\"~\\");
        term.setWasMapped(true);

        TermQuery tq = new TermQuery(term);
        String qs = buildQueryString(qb, tq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different query than expected",
            "multipart_link:\\|\\!\\&urn\\:nbn\\:de\\:bsz\\:\\{14\\}\\-\\[qucosa\\]\\-\\(108346\\)\\!\\^\\\"\\~\\\\", qs);

        qb = QueryBuilder.Factory.getInstance();
        PhraseQuery pq = new PhraseQuery(term);
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different query than expected",
            "multipart_link:\"|!&urn:nbn:de:bsz:{14}-[qucosa]-(108346)!^\\\"~\\\\\"", qs);

        qb = QueryBuilder.Factory.getInstance();
        term = Term.newSearchServerTerm("title", "text space");
        term.setWasMapped(true);
        qs = buildQueryString(qb, new TermQuery(term), cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different query than expected", "title:\"text space\"", qs);

        QueryBuilder builder = (QueryBuilder) QueryBuilder.Factory.getInstance();
        qs = builder.escape(builder.getEscapePattern(), "a:\\?\\*:?:*:b");
        Assert.assertEquals("Got different query than expected", "a\\:\\?\\*\\:?\\:*\\:b", qs);
    }
}
