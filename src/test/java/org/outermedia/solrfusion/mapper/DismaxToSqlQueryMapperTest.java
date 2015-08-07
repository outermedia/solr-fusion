package org.outermedia.solrfusion.mapper;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 04.06.14.
 */
public class DismaxToSqlQueryMapperTest
{
    protected TestHelper helper;
    protected Configuration cfg;
    protected SearchServerConfig searchServerConfig;
    protected ScriptEnv env;
    protected Locale locale = Locale.GERMAN;

    @Before
    public void setup() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        helper = new TestHelper();
        cfg = helper.readFusionSchemaWithoutValidation("fusion-schema-sql.xml");
        searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);
        env = new ScriptEnv();
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
    }

    @Test
    public void testSimpleQueryMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("username", "user1"));
        setFusionField(q);
        qm.mapQuery(cfg, searchServerConfig, q, env, null, QueryTarget.ALL);

        String expected =
                "Term(fusionFieldName=username, fusionFieldValue=[user1], fusionField=FusionField(fieldName=username, type=text, format=null, multiValue=null), searchServerFieldName=dbatest.de_sball_model_PersonIfc.username, searchServerFieldValue=[user1], removed=false, wasMapped=true, processed=true, newQueries=null)";
        Assert.assertEquals("Got different mapping than expected", expected, q.getTerm().toString());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, q, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Found wrong search server term query mapping",
                "`dbatest`.`de_sball_model_PersonIfc`.`username` like '%user1%'", s);
    }

    private QueryBuilderIfc getDismaxToMysqlQb() throws InvocationTargetException, IllegalAccessException
    {
        return searchServerConfig.getDismaxQueryBuilder(cfg.getDismaxQueryBuilder());
    }

    private void setFusionField(TermQuery q)
    {
        FusionField field = cfg.findFieldByName(q.getFusionFieldName());
        q.getTerm().setFusionField(field);
    }

    protected String buildQueryString(QueryBuilderIfc qb, Query bq, Configuration cfg,
                                      SearchServerConfig searchServerConfig, Locale locale,
                                      Set<String> defaultSearchServerFields, QueryTarget target)
    {
        String qs = qb.buildQueryString(bq, cfg, searchServerConfig, Locale.GERMAN, defaultSearchServerFields, target);
        qs = qb.getStaticallyAddedQueries(cfg, searchServerConfig, Locale.GERMAN, target, qs);
        return qs;
    }

    protected void setField(Term fusionTerm)
    {
        fusionTerm.setFusionField(cfg.findFieldByName(fusionTerm.getFusionFieldName()));
    }

    @Test
    public void testQueryConjunctionMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("username", "user1"));
        setField(q.getTerm());
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("name", "User One"));
        setField(q2.getTerm());
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);

        String expectedAuthor =
                "Term(fusionFieldName=username, fusionFieldValue=[user1], fusionField=FusionField(fieldName=username, type=text, format=null, multiValue=null), searchServerFieldName=dbatest.de_sball_model_PersonIfc.username, searchServerFieldValue=[user1], removed=false, wasMapped=true, processed=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle =
                "Term(fusionFieldName=name, fusionFieldValue=[User One], fusionField=FusionField(fieldName=name, type=text, format=null, multiValue=null), searchServerFieldName=dbatest.de_sball_model_PersonIfc.name, searchServerFieldValue=[User One], removed=false, wasMapped=true, processed=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Found wrong search server bool query mapping",
                "(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%user1%' AND `dbatest`.`de_sball_model_PersonIfc`.`name` like '%User One%')",
                s.trim());
    }

    @Test
    public void testQueryDisjunctionMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("username", "Schiller"));
        setField(q.getTerm());
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST_NOT));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("name", "Ein_langer_Weg"));
        setField(q2.getTerm());
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST_NOT));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);

        String expectedAuthor =
                "Term(fusionFieldName=username, fusionFieldValue=[Schiller], fusionField=FusionField(fieldName=username, type=text, format=null, multiValue=null), searchServerFieldName=dbatest.de_sball_model_PersonIfc.username, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, processed=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle =
                "Term(fusionFieldName=name, fusionFieldValue=[Ein_langer_Weg], fusionField=FusionField(fieldName=name, type=text, format=null, multiValue=null), searchServerFieldName=dbatest.de_sball_model_PersonIfc.name, searchServerFieldValue=[Ein_langer_Weg], removed=false, wasMapped=true, processed=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Found wrong search server bool query mapping",
                "(!(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%Schiller%') AND !(`dbatest`.`de_sball_model_PersonIfc`.`name` like '%Ein_langer_Weg%'))",
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
    public void testAllDocsQueryHandling() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException, InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        MatchAllDocsQuery wq = new MatchAllDocsQuery();
        qm.mapQuery(cfg, searchServerConfig, wq, env, null, QueryTarget.ALL);

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, wq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        // not supported by dismax
        Assert.assertEquals("Expected match all docs query", "", s);
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void testFuzzyQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("username", "abc");
        setField(term);
        FuzzyQuery fq = new FuzzyQuery(term, null);
        qm.mapQuery(cfg, searchServerConfig, fq, env, null, QueryTarget.ALL);
        Assert.assertTrue("Term not mapped", term.isWasMapped());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, fq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        // not supported by dismax
        Assert.assertEquals("Expected match all docs query", "", s);
    }

    @Test
    public void testPhraseQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("username", "abc");
        setField(term);
        PhraseQuery pq = new PhraseQuery(term);
        qm.mapQuery(cfg, searchServerConfig, pq, env, null, QueryTarget.ALL);
        Assert.assertTrue("Term not mapped", term.isWasMapped());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Expected match all docs query",
                "`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%'", s);
    }

    @Test
    public void testPrefixQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("username", "abc*");
        setField(term);
        PrefixQuery pq = new PrefixQuery(term);
        qm.mapQuery(cfg, searchServerConfig, pq, env, null, QueryTarget.ALL);
        Assert.assertTrue("Term not mapped", term.isWasMapped());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        // not supported by dismax
        Assert.assertEquals("Expected match all docs query", "", s);
    }

    @Test
    public void testWildcardQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("username", "a*bc*");
        setField(term);
        WildcardQuery pq = new WildcardQuery(term);
        qm.mapQuery(cfg, searchServerConfig, pq, env, null, QueryTarget.ALL);
        Assert.assertTrue("Term not mapped", term.isWasMapped());

        QueryBuilderIfc qb = getDismaxToMysqlQb();
        String s = buildQueryString(qb, pq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        // not supported by dismax
        Assert.assertEquals("Expected match wildcard query", "", s);
    }

    @Test
    public void testRangeQueries() throws InvocationTargetException, IllegalAccessException
    {
        NumericRangeQuery<?> queryObjects[] = {NumericRangeQuery
                .newDateRange("birthday", new GregorianCalendar(2014, 5, 26), new GregorianCalendar(2014, 6, 1), true,
                        true), NumericRangeQuery.newDoubleRange("failLoginNr", 2.5, 4.5, true, true),
                NumericRangeQuery.newFloatRange("failLoginNr", 2.5f, 4.5f, true, true),
                NumericRangeQuery.newIntRange("failLoginNr", 2, 4, true, true),
                NumericRangeQuery.newLongRange("failLoginNr", 2L, 4L, true, true)};
        // not supported by dismax
        String expectedSql[] = {"", "", "", "", ""};
        int at = 0;
        for (int i = 0; i < queryObjects.length; i++)
        {
            NumericRangeQuery<?> nq = queryObjects[i];
            QueryMapperIfc qm = cfg.getQueryMapper();
            Term minMax = nq.getTerm();
            setField(minMax);
            minMax.newFusionTerm("title");
            minMax.setFusionFieldValue(minMax.getFusionFieldValue());
            qm.mapQuery(cfg, searchServerConfig, nq, env, null, QueryTarget.ALL);
            Assert.assertTrue("Term not mapped", minMax.isWasMapped());

            QueryBuilderIfc qb = getDismaxToMysqlQb();
            String s = buildQueryString(qb, nq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
            Assert.assertEquals("Expected match query #" + i, expectedSql[i], s);
        }
    }

    protected BooleanClause createShouldBooleanClause(Query q)
    {
        return new BooleanClause(q, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("username", s);
        setField(term);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST);
    }

    protected BooleanClause createShouldBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("username", s);
        setField(term);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_SHOULD);
    }

    protected BooleanClause createMustNotBooleanClause(String s, List<Term> terms)
    {
        Term term = Term.newFusionTerm("username", s);
        setField(term);
        terms.add(term);
        TermQuery tq = new TermQuery(term);
        return new BooleanClause(tq, BooleanClause.Occur.OCCUR_MUST_NOT);
    }

    @Test
    public void testBoolQuery() throws InvocationTargetException, IllegalAccessException
    {
        List<Term> termList = new ArrayList<>();

        // empty case
        QueryMapperIfc qm = cfg.getQueryMapper();
        BooleanQuery bq = new BooleanQuery();
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);

        String s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected", "", s);

        // one MUST clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected", "(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%')",
                s);

        // one SHOULD clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%' OR true)", s);

        // one MUST NOT clause
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "(!(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%'))", s);

        // several MUST clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustBooleanClause("abc", termList));
        bq.add(createMustBooleanClause("def", termList));
        bq.add(createMustNotBooleanClause("ghi", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%' AND `dbatest`.`de_sball_model_PersonIfc`.`username` like '%def%' AND !(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%ghi%'))",
                s);

        // several SHOULD clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createShouldBooleanClause("abc", termList));
        bq.add(createShouldBooleanClause("def", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%' " +
                        "OR `dbatest`.`de_sball_model_PersonIfc`.`username` like '%def%' OR true)", s);

        // several MUST NOT clauses
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList));
        bq.add(createMustNotBooleanClause("def", termList));
        bq.add(createMustBooleanClause("ghi", termList));
        bq.add(createShouldBooleanClause("jkl", termList));
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "((!(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%') " +
                        "AND !(`dbatest`.`de_sball_model_PersonIfc`.`username` like '%def%') " +
                        "AND `dbatest`.`de_sball_model_PersonIfc`.`username` like '%ghi%') AND (`dbatest`.`de_sball_model_PersonIfc`.`username` like '%jkl%' OR true))",
                s);

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
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected",
                "((`dbatest`.`de_sball_model_PersonIfc`.`username` like '%abc%' AND `dbatest`.`de_sball_model_PersonIfc`.`username` like '%def%') " +
                        "OR (`dbatest`.`de_sball_model_PersonIfc`.`username` like '%ghi%' AND `dbatest`.`de_sball_model_PersonIfc`.`username` like '%jkl%') OR true)",
                s);

        // several deleted clauses
        /* TODO
        qm = cfg.getQueryMapper();
        bq = new BooleanQuery();
        termList.clear();
        bq.add(createMustNotBooleanClause("abc", termList)); // del
        bq.add(createMustBooleanClause("def", termList));
        bq.add(createShouldBooleanClause("ghi", termList)); // del
        bq.add(createShouldBooleanClause("jkl", termList));
        bq.add(createMustBooleanClause("mno", termList)); // del
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        s = buildQueryString(getDismaxToMysqlQb(), bq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected","",s);

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
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
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
        qm.mapQuery(cfg, searchServerConfig, bq, env, null, QueryTarget.ALL);
        checkMapped(termList);
        */
    }

    protected void checkMapped(List<Term> terms)
    {
        for (Term t : terms)
        {
            Assert.assertTrue("Term not mapped", t.isWasMapped());
        }
    }

    @Test
    public void testSubQuery() throws InvocationTargetException, IllegalAccessException
    {
        QueryMapperIfc qm = cfg.getQueryMapper();
        Term term = Term.newFusionTerm("username", "abc*");
        setField(term);
        WildcardQuery pq = new WildcardQuery(term);
        MetaInfo mi = new MetaInfo();
        pq.setMetaInfo(mi);
        mi.addFusionEntry("qf", "username^1.1 name^2.1");
        SubQuery sq = new SubQuery(pq);
        qm.mapQuery(cfg, searchServerConfig, sq, env, new FusionRequest(), QueryTarget.ALL);
        Assert.assertTrue("Term not mapped", term.isWasMapped());
        Map<String, String> mappedParams = mi.getSearchServerParameterMap();
        Assert.assertEquals("Expected other mapping",
                "dbatest.de_sball_model_PersonIfc.username^1.1 dbatest.de_sball_model_PersonIfc.name^2.1",
                mappedParams.get("qf"));

        String s = buildQueryString(getDismaxToMysqlQb(), sq, cfg, searchServerConfig, locale, null, QueryTarget.ALL);
        Assert.assertEquals("Other sql than expected", "(`dbatest`.`de_sball_model_PersonIfc`.`username` like 'abc%')",
                s);
    }

}
