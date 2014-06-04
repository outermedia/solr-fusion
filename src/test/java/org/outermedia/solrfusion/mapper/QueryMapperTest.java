package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.query.parser.BooleanClause;
import org.outermedia.solrfusion.query.parser.BooleanQuery;
import org.outermedia.solrfusion.query.parser.Term;
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
        TermQuery q = new TermQuery(new Term("author", "Hans Schiller"));
        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), q, env);

        String expected = "Term(fusionFieldName=author, fusionFieldValue=Hans Schiller, fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=Hans Schiller, removed=false, wasMapped=true, newQueries=null)";
        Assert.assertEquals("Got different mapping than expected", expected, q.getTerm().toString());
    }

    @Test
    public void testQueryConjunctionMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        QueryMapper qm = new QueryMapper();
        TermQuery q = new TermQuery(new Term("author", "Hans Schiller"));
        ScriptEnv env = new ScriptEnv();
        BooleanQuery bq = new BooleanQuery(false);
        bq.add(new BooleanClause(q, BooleanClause.Occur.OCCUR_MUST));
        TermQuery q2 = new TermQuery(new Term("title", "Ein langer Weg"));
        bq.add(new BooleanClause(q2, BooleanClause.Occur.OCCUR_MUST));
        qm.mapQuery(cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), bq, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=Hans Schiller, fusionField=null, searchServerFieldName=Autor, searchServerFieldValue=Hans Schiller, removed=false, wasMapped=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped author.", expectedAuthor, q.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=Ein langer Weg, fusionField=null, searchServerFieldName=Titel, searchServerFieldValue=Ein langer Weg, removed=false, wasMapped=true, newQueries=null)";
        Assert.assertEquals("Didn't find mapped title.", expectedTitle, q2.getTerm().toString());
    }
}
