package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrField;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    public void testSimpleResponseMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-query-mapper-fusion-schema.xml");
        ResponseMapper rm = new ResponseMapper();
        Document doc = new Document();
        List<SolrField> strFields = new ArrayList<>();
        SolrField sfTitle = new SolrField();
        sfTitle.setFieldName("Titel");
        sfTitle.setValue("Ein kurzer Weg");
        sfTitle.setTerm(Term.newSearchServerTerm(sfTitle.getFieldName(), sfTitle.getValue()));
        strFields.add(sfTitle);
        SolrField sfAuthor = new SolrField();
        sfAuthor.setFieldName("Autor");
        sfAuthor.setValue("Willi Schiller");
        sfAuthor.setTerm(Term.newSearchServerTerm(sfAuthor.getFieldName(), sfAuthor.getValue()));
        strFields.add(sfAuthor);
        doc.setSolrStringFields(strFields);
        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);

        String expectedAuthor = "Term(fusionFieldName=author, fusionFieldValue=Willi Schiller, fusionField=FusionField(fieldName=author, type=string, format=null), searchServerFieldName=Autor, searchServerFieldValue=Willi Schiller, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Mapping of author returned different result.", expectedAuthor, sfAuthor.getTerm().toString());

        String expectedTitle = "Term(fusionFieldName=title, fusionFieldValue=Ein kurzer Weg, fusionField=FusionField(fieldName=title, type=string, format=null), searchServerFieldName=Titel, searchServerFieldValue=Ein kurzer Weg, removed=false, wasMapped=true, newTerms=null)";
        Assert.assertEquals("Mapping of title returned different result.", expectedTitle, sfTitle.getTerm().toString());
    }
}
