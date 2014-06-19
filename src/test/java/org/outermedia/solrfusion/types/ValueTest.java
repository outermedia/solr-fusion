package org.outermedia.solrfusion.types;

import org.junit.Assert;
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
import java.util.List;

/**
 * Created by ballmann on 6/19/14.
 */
public class ValueTest extends AbstractTypeTest
{

    @Test
    public void testSingleValue() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<value>abc123</value>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = new Value();
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", Arrays.asList("abc123"), values);

        values = valueType.apply(null, null);
        Assert.assertEquals("Found different values than expected", Arrays.asList("abc123"), values);
    }

    @Test
    public void testSeveralValues() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<value>a</value><value>b</value><value>c</value>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = new Value();
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", Arrays.asList("a", "b", "c"), values);

        values = valueType.apply(null, null);
        Assert.assertEquals("Found different values than expected", Arrays.asList("a", "b", "c"), values);
    }

    @Test
    public void testNoValue() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = new Value();
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", null, values);

        values = valueType.apply(null, null);
        Assert.assertEquals("Found different values than expected", null, values);
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
        Term sourceField = buildResponseField(doc, "bibName", "dfskjfsdk7");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "source", sourceField.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("BIB-A"),
                sourceField.getFusionFieldValue());
    }

}
