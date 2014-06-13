package org.outermedia.solrfusion.query;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.Query;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class QueryTest
{

	protected TestHelper helper;

	@Before
	public void setup()
	{
		helper = new TestHelper();
	}

    @Test
    public void parseWordQuery() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "Schiller";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        Query o = p.parse(cfg, boosts, query);
        Assert.assertNotNull(
                "Expected query object, but couldn't parse query string '" + query
                        + "'", o);
        String expected = "TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=Schiller, fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newTerms=null))";
        Assert.assertEquals("Got different query object than expected",
                expected, o.toString());
    }

	@Test
	public void parseTermQuery() throws FileNotFoundException, JAXBException,
		SAXException, ParserConfigurationException
	{
		Configuration cfg = helper
			.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

		String query = "title:Schiller";
		EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
		p.init(new QueryParserFactory());
		Map<String, Float> boosts = new HashMap<String, Float>();
		Query o = p.parse(cfg, boosts, query);
		Assert.assertNotNull(
			"Expected query object, but couldn't parse query string '" + query
				+ "'", o);
		String expected = "TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=Schiller, fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newTerms=null))";
		Assert.assertEquals("Got different query object than expected",
			expected, o.toString());
    }

    @Test
    public void parseTermConjunctionQuery() throws FileNotFoundException, JAXBException,
            SAXException, ParserConfigurationException
    {
        Configuration cfg = helper
                .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

        String query = "title:Schiller title:Müller";
        EdisMaxQueryParser p = EdisMaxQueryParser.Factory.getInstance();
        p.init(new QueryParserFactory());
        Map<String, Float> boosts = new HashMap<String, Float>();
        // check diacritical chars
        Query o = p.parse(cfg, boosts, query);
        Assert.assertNotNull(
                "Expected query object, but couldn't parse query string '" + query
                        + "'", o);
        String expected = "BooleanQuery(super=Query(), clauses=[BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=Schiller, fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newTerms=null))), BooleanClause(occur=OCCUR_MUST, query=TermQuery(super=Query(), term=Term(fusionFieldName=title, fusionFieldValue=Müller, fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=null, searchServerFieldValue=null, removed=false, wasMapped=false, newTerms=null)))])";
        Assert.assertEquals("Got different query object than expected",
                expected, o.toString());
    }
}
