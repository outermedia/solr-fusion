package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.query.parser.BooleanClause;
import org.outermedia.solrfusion.query.parser.BooleanQuery;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;

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
    public void testSimpleQueryMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapper qm = new QueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), q, env);

        String expected = "Term(fusionFieldName=author, fusionFieldValue=Schiller, fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=Schiller, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Got different mapping than expected", expected, q.getTerm().toString());

        SearchServerQueryBuilder qb = new SearchServerQueryBuilder();
        String s = qb.buildQueryString(q);
        Assert.assertEquals("Found wrong search server term query mapping", "Autor:Schiller", s);
    }

    @Test
    public void testQueryConjunctionMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapper qm = new QueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        BooleanQuery bq = new BooleanQuery(false);
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST));
        qm.mapQuery(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=Schiller, fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=Schiller, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=Ein_langer_Weg, fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=Ein_langer_Weg, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        SearchServerQueryBuilder qb = new SearchServerQueryBuilder();
        String s = qb.buildQueryString(bq);
        Assert.assertEquals("Found wrong search server bool query mapping", "+Autor:Schiller +Titel:Ein_langer_Weg", s.trim());
    }

    @Test
    public void testQueryDisjunctionMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapper qm = new QueryMapper();
        TermQuery q = new TermQuery(Term.newFusionTerm("author", "Schiller"));
        ScriptEnv env = new ScriptEnv();
        BooleanQuery bq = new BooleanQuery(false);
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST_NOT));
        TermQuery q2 = new TermQuery(Term.newFusionTerm("title", "Ein_langer_Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST_NOT));
        qm.mapQuery(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=Schiller, fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=Schiller, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=Ein_langer_Weg, fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=Ein_langer_Weg, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());

        SearchServerQueryBuilder qb = new SearchServerQueryBuilder();
        String s = qb.buildQueryString(bq);
        Assert.assertEquals("Found wrong search server bool query mapping", "-Autor:Schiller -Titel:Ein_langer_Weg", s.trim());

        ResetQueryState resetter = new ResetQueryState();
        resetter.reset(bq);
        Assert.assertNull("Expected empty fusion field name after reset", q.getSearchServerFieldName());
        Assert.assertNull("Expected empty fusion field value after reset", q.getSearchServerFieldValue());
        Assert.assertFalse("Expected false for removed after reset", q.getTerm().isRemoved());
        Assert.assertFalse("Expected false for wasMapped after reset", q.getTerm().isWasMapped());
        Assert.assertNull("Expected empty fusion field name after reset", q2.getSearchServerFieldName());
        Assert.assertNull("Expected empty fusion field value after reset", q2.getSearchServerFieldValue());
        Assert.assertFalse("Expected false for removed after reset", q2.getTerm().isRemoved());
        Assert.assertFalse("Expected false for wasMapped after reset", q2.getTerm().isWasMapped());
    }
}
