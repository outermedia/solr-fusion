package org.outermedia.solrfusion.types;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
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
import java.util.regex.Pattern;

/**
 * Created by ballmann on 6/19/14.
 */
public class RegularExpressionTest extends AbstractTypeTest
{
    @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<pattern>([^,]+),\\s*(.+)</pattern><replacement>$2 $1</replacement>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        RegularExpression rxType = Mockito.spy(new RegularExpression());
        rxType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(rxType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        String pattern = rxType.getPattern().pattern();
        String replacement = rxType.getReplacement();
        Assert.assertEquals("Parsing of pattern failed.", "([^,]+),\\s*(.+)", pattern);
        Assert.assertEquals("Parsing of replacement failed.", "$2 $1", replacement);
    }

    @Test
    public void testMissingConfig() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        RegularExpression rxType = Mockito.spy(new RegularExpression());
        rxType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(rxType, Mockito.times(1)).logBadConfiguration(Mockito.eq(false), Mockito.anyList());

        Pattern pattern = rxType.getPattern();
        String replacement = rxType.getReplacement();
        Assert.assertNull("Handling of missing pattern failed.", pattern);
        Assert.assertNull("Handling of missing replacement failed.", replacement);
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
        Term sourceField = buildResponseField(doc, "f5", "Müller, Thomas", "Lahm, Philipp");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env);
        // System.out.println(sourceField.toString());
        org.junit.Assert.assertEquals("Found wrong field name mapping", "text1", sourceField.getFusionFieldName());
        org.junit.Assert.assertEquals("Found wrong field value mapping", Arrays.asList("Thomas Müller", "Philipp Lahm"),
                sourceField.getFusionFieldValue());
    }
}
