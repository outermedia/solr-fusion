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
import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class QueryMapperTest
{
    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void testSimpleQueryMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), q, env);

        String expected = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
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
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=[Ein_langer_Weg], fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=[Ein_langer_Weg], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Found wrong search server bool query mapping", "+Autor:Schiller +Titel:Ein_langer_Weg",
                s.trim());
    }

    @Test
    public void testQueryDisjunctionMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        BooleanQuery bq = new BooleanQuery();
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST_NOT));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST_NOT));
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=[Schiller], fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=[Schiller], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=[Ein_langer_Weg], fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=[Ein_langer_Weg], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(bq, cfg);
        Assert.assertEquals("Found wrong search server bool query mapping", "-Autor:Schiller -Titel:Ein_langer_Weg",
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
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapperIfc qm = cfg.getQueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("valueFrom7", "Schiller"));
        ScriptEnv env = new ScriptEnv();
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
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapperIfc qm = cfg.getQueryMapper();
        MatchAllDocsQuery wq = new MatchAllDocsQuery();
        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), wq, env);

        QueryBuilderIfc qb = cfg.getDefaultQueryBuilder();
        String s = qb.buildQueryString(wq, cfg);
        Assert.assertEquals("Expected match all docs query", "*:*", s);
    }
}
