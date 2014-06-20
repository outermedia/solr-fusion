package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class ResponseMapperTest
{
    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void testSimpleResponseMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = new Document();
        List<SolrSingleValuedField> strFields = new ArrayList<>();
        SolrSingleValuedField sfTitle = new SolrSingleValuedField();
        sfTitle.setFieldName("Titel");
        sfTitle.setValue("Ein kurzer Weg");
        sfTitle.setTerm(Term.newSearchServerTerm(sfTitle.getFieldName(), sfTitle.getValue()));
        strFields.add(sfTitle);
        SolrSingleValuedField sfAuthor = new SolrSingleValuedField();
        sfAuthor.setFieldName("Autor");
        sfAuthor.setValue("Willi Schiller");
        sfAuthor.setTerm(Term.newSearchServerTerm(sfAuthor.getFieldName(), sfAuthor.getValue()));
        strFields.add(sfAuthor);
        SolrSingleValuedField sfId = new SolrSingleValuedField();
        sfId.setFieldName("id");
        sfId.setValue("132");
        sfId.setTerm(Term.newSearchServerTerm(sfId.getFieldName(), sfId.getValue()));
        strFields.add(sfId);
        doc.setSolrSingleValuedFields(strFields);
        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=[Willi Schiller], fusionField=FusionField(fieldName=author, type=text, format=null), searchServerFieldName=Autor, searchServerFieldValue=[Willi Schiller], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Mapping of author returned different result.", expectedAuthor,
                sfAuthor.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=[Ein kurzer Weg], fusionField=FusionField(fieldName=title, type=text, format=null), searchServerFieldName=Titel, searchServerFieldValue=[Ein kurzer Weg], removed=false, wasMapped=true, newQueryTerms=null, newResponseValues=null)";
        Assert.assertEquals("Mapping of title returned different result.", expectedTitle, sfTitle.getTerm().toString());
    }

    @Test
    public void testRegExpr() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException,
            InvocationTargetException, IllegalAccessException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        Document doc = new Document();
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        List<SolrSingleValuedField> strFields = new ArrayList<>();
        SolrSingleValuedField sfTitle = new SolrSingleValuedField();
        sfTitle.setFieldName("val7Start");
        sfTitle.setValue("Ein kurzer Weg");
        sfTitle.setTerm(Term.newSearchServerTerm(sfTitle.getFieldName(), sfTitle.getValue()));
        strFields.add(sfTitle);
        SolrSingleValuedField sfId = new SolrSingleValuedField();
        sfId.setFieldName("id");
        sfId.setValue("132");
        sfId.setTerm(Term.newSearchServerTerm(sfId.getFieldName(), sfId.getValue()));
        strFields.add(sfId);
        doc.setSolrSingleValuedFields(strFields);
        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);

        Term term = sfTitle.getTerm();
        String fusionFieldName = term.getFusionFieldName();
        // ensure that "valueFrom7" is declared as <om:fusion-schema-fields><om:field>!
        Assert.assertEquals("RegExp mapping returned different fusion field than expected", "valueFrom7",
                fusionFieldName);
        List<String> fusionFieldValue = term.getFusionFieldValue();
        Assert.assertEquals("RegExp mapping returned different search server field value than expected",
                Arrays.asList("Ein kurzer Weg"), fusionFieldValue);
    }
}
