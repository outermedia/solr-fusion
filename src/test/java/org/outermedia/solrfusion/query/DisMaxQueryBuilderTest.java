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
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.DisMaxQueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilder;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 7/2/14.
 */
@Slf4j
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
        String qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different fuzzy query than expected", "", qs);
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
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        MatchAllDocsQuery fq = new MatchAllDocsQuery();
        String qs = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different *:* query than expected", "", qs);
    }

    @Test
    public void testPhraseQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryBuilderIfc qb = getDismaxQueryBuilder();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"),
            QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"", qs);

        qb = getDismaxQueryBuilder();
        term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        pq = new PhraseQuery(term);
        pq.setBoost(3.5f);
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"^3.5", qs);
    }

    @Test
    public void testPrefixQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc*");
        term.setWasMapped(true);
        PrefixQuery pq = new PrefixQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different prefix query than expected", "", qs);
    }

    @Test
    public void testWildcardQuery()
    {
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        WildcardQuery pq = new WildcardQuery(term);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
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
            String qs = buildQueryString(qb, nq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
            Assert.assertEquals("Got different min value than expected", "", qs);
        }
    }

    @Test
    public void testBoolQuery() throws InvocationTargetException, IllegalAccessException
    {
        // empty case
        QueryBuilderIfc qb = DisMaxQueryBuilder.Factory.getInstance();
        BooleanQuery bq = new BooleanQuery();
        String qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "", qs);

        // one MUST clause
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "+abc", qs);

        // one SHOULD clause
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "abc", qs);

        // one MUST NOT clause
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "-abc", qs);

        // several MUST clauses
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createMustBooleanClause("abc", true));
        bq.add(createMustBooleanClause("def", true));
        bq.add(createMustNotBooleanClause("ghi", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "+abc +def -ghi", qs);

        // several SHOULD clauses
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause("abc", true));
        bq.add(createShouldBooleanClause("def", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "abc def", qs);

        // several MUST NOT clauses
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", true));
        bq.add(createMustNotBooleanClause("def", true));
        bq.add(createMustBooleanClause("ghi", true));
        bq.add(createShouldBooleanClause("jkl", true));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "-abc -def +ghi jkl", qs);

        // nested bool queries
        qb = getDismaxQueryBuilder();
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", true));
        bq2.add(createMustBooleanClause("jkl", true));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "+abc +def +ghi +jkl", qs);

        // several deleted clauses
        qb = getDismaxQueryBuilder();
        bq = new BooleanQuery();
        bq.add(createMustNotBooleanClause("abc", false)); // del
        bq.add(createMustBooleanClause("def", true));
        bq.add(createShouldBooleanClause("ghi", false)); // del
        bq.add(createShouldBooleanClause("jkl", true));
        bq.add(createMustBooleanClause("mno", false)); // del
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "+def jkl", qs);

        // delete whole bool query
        qb = getDismaxQueryBuilder();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "+abc +def", qs);

        // delete all bool queries
        qb = getDismaxQueryBuilder();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", false));
        bq1.add(createMustBooleanClause("def", false));
        bq2 = new BooleanQuery();
        bq2.add(createMustBooleanClause("ghi", false));
        bq2.add(createMustBooleanClause("jkl", false));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createShouldBooleanClause(bq2));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different bool query than expected", "", qs);

        // remove duplicate search words
        qb = getDismaxQueryBuilder();
        bq1 = new BooleanQuery();
        bq1.add(createMustBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        bq1.add(createShouldBooleanClause("ghi", true));
        bq1.add(createShouldBooleanClause("abc", true));
        bq1.add(createMustBooleanClause("def", true));
        bq2 = new BooleanQuery();
        bq2.add(createMustNotBooleanClause("jkl", true));
        bq2.add(createMustNotBooleanClause("abc", true));
        bq2.add(createMustNotBooleanClause("def", true));
        bq2.add(createShouldBooleanClause("ghi", true));
        bq2.add(createMustNotBooleanClause("jkl", true));
        bq2.add(createMustNotBooleanClause("mno", true));
        bq = new BooleanQuery();
        bq.add(createShouldBooleanClause(bq1));
        bq.add(createMustBooleanClause(bq2));
        qs = buildQueryString(qb, bq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Expected the removal of duplicate search words", "+abc +def ghi +(-jkl -mno)", qs);
    }

    protected QueryBuilderIfc getDismaxQueryBuilder() throws InvocationTargetException, IllegalAccessException
    {
        return cfg.getDismaxQueryBuilder();
    }

    protected BooleanClause createShouldBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST);
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
    public void testSubQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryBuilderIfc qb = getDismaxQueryBuilder();
        Term term = Term.newSearchServerTerm("title", "abc?");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        SubQuery sq = new SubQuery(pq);
        String qs = buildQueryString(qb, sq, cfg, searchServerConfig, locale, Sets.newHashSet("title"),
            QueryTarget.ALL);
        Assert.assertEquals("Got different sub query than expected", "\"abc?\"", qs);
    }

    @Test
    public void testMetaInfo() throws InvocationTargetException, IllegalAccessException
    {
        QueryBuilderIfc qb = getDismaxQueryBuilder();
        Term term = Term.newSearchServerTerm("title", "abc");
        term.setWasMapped(true);
        PhraseQuery pq = new PhraseQuery(term);
        MetaInfo mi = new MetaInfo();
        mi.setSearchServerParams(new MetaParams());
        mi.addSearchServerEntry("a", "1");
        pq.setMetaInfo(mi);
        String qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"),
            QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}\"abc\"", qs);

        // name set in MetaInfo
        qb = getDismaxQueryBuilder();
        mi.setName("dismax");
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"", qs);

        // name not set, but value
        qb = getDismaxQueryBuilder();
        mi.setName(null);
        mi.setValue("abc");
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "{!a=1}\"abc\"", qs);

        // name, value and params set
        mi.setName("dismax");
        qb = getDismaxQueryBuilder();
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "\"abc\"", qs);

        // several params set
        qb = getDismaxQueryBuilder();
        mi.setName(null);
        mi.setValue(null);
        mi.addSearchServerEntry("a", "1");
        mi.addSearchServerEntry("b", "2");
        qs = buildQueryString(qb, pq, cfg, searchServerConfig, locale, Sets.newHashSet("title"), QueryTarget.ALL);
        Assert.assertEquals("Got different phrase query than expected", "{!a=1 b=2}\"abc\"", qs);
    }

    @Test
    public void testEdismaxToDismax()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, ParseException,
        InvocationTargetException, IllegalAccessException
    {
        String qs = "((_query_:\"{!dismax qf=\\\"author^200 author_id^100 author_ref^150 author_corp^200 " +
            "author_corp_ref^150 author_orig^200 author2_orig^200 author_corp_orig^200 author_corp2_orig^200 " +
            "author2 author_additional\\\"}Schiller\") AND (_query_:\"{!dismax qf=\\\"title_full_unstemmed^150 " +
            "title_full^100 title^900 title_alt^200 title_new^100 title_old title_orig^400 series^100 series2 " +
            "series_orig^100\\\"}Räuber\"))";
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-uni-leipzig.xml");
        searchServerConfig = cfg.getSearchServerConfigByName("DBoD2");
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query q = p.parse(cfg, boosts, qs, Locale.GERMAN, null);
        ScriptEnv env = new ScriptEnv();
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
        env.setBinding(ScriptEnv.ENV_DISMAX_WORD_CACHE, new HashSet<String>());
        FusionRequest fusionRequest = new FusionRequest();
        cfg.getQueryMapper().mapQuery(cfg, searchServerConfig, q, env, fusionRequest, QueryTarget.ALL);
        Set<String> defaultSearchServerSearchFields = fusionRequest.mapFusionFieldToSearchServerField(
            cfg.getDefaultSearchField(), cfg, searchServerConfig, null, QueryTarget.ALL);

        // at first build edismax query string
        String eqs = buildQueryString(QueryBuilder.Factory.getInstance(), q, cfg, searchServerConfig, Locale.GERMAN,
            defaultSearchServerSearchFields, QueryTarget.ALL);
        // System.out.println("Edismax Query: " + eqs);

        // then build dismax query string
        String dqs = buildQueryString(DisMaxQueryBuilder.Factory.getInstance(), q, cfg, searchServerConfig,
            Locale.GERMAN, defaultSearchServerSearchFields, QueryTarget.ALL);
        // System.out.println("Dismax Query: " + dqs);
        String expected = "+Schiller +Räuber";
        Assert.assertEquals("Expected other dismax query string", expected, dqs);
    }
}
