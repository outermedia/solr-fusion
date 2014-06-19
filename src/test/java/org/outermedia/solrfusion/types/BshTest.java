package org.outermedia.solrfusion.types;

import junit.framework.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapper;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ballmann on 6/19/14.
 */
public class BshTest extends AbstractTypeTest
{
    @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<script>return 42;</script>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Bsh bshType = new Bsh();
        bshType.passArguments(util.filterElements(elem.getChildNodes()), util);

        String code = bshType.getCode();
        Assert.assertEquals("Parsing of configuration failed.", "return 42;", code);
    }

    @Test
    public void testMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapper rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        buildResponseField(doc, "Titel", "Ein kurzer Weg");
        buildResponseField(doc, "Autor", "Willi Schiller");
        buildResponseField(doc, "id", "132");
        Term sourceField = buildResponseField(doc, "f1", "something");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);
        // System.out.println(sourceField.toString());
        org.junit.Assert.assertEquals("Found wrong field name mapping", "today", sourceField.getFusionFieldName());
        org.junit.Assert.assertEquals("Found wrong field value mapping", Arrays.asList("something at 2014-07-19"),
                sourceField.getFusionFieldValue());
    }
}
