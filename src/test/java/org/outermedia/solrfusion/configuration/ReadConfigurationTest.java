package org.outermedia.solrfusion.configuration;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.types.Bsh;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class ReadConfigurationTest
{
    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void readFusionSchema() throws JAXBException, SAXException,
        ParserConfigurationException, IOException
    {
        // one xml file which contains servers too
        String config1Out = addNewlines(helper.readFusionSchemaWithValidation(
            "test-fusion-schema.xml", "configuration.xsd").toString());

        // this configuration uses <xi:include> to include server declarations
        String config2Out = addNewlines(helper.readFusionSchemaWithValidation(
            "test-global-fusion-schema.xml", "configuration.xsd").toString());

        Assert.assertEquals(
            "<xi:include> should work transparently, but differences occurred",
            addNewlines(config1Out), (config2Out));

        String expected = Files
            .toString(new File("src/test/resources/schema-toString.txt"),
                Charset.forName("UTF-8"));

        expected = addNewlines(expected);
        // System.out.println(config2Out);

        Assert.assertEquals("Found different configuration", expected,
            config2Out);
    }

    protected String addNewlines(String s)
    {
        return s.replace("[", "[\n\t").replace("),", "),\n\t")
            .replaceAll("\\n\\s+", "\n ");
    }

    @Test
    public void checkXpath() throws FileNotFoundException, JAXBException,
        SAXException, ParserConfigurationException, XPathExpressionException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");

		/*
            <om:query type="beanshell">
		        <script><![CDATA[
		        	currentQuery = 
		        		currentQuery.replace("XXX",System.currentTimeMillis());
		        ]]></script>
		    </om:query>
		 */
        Target beanShellQuery = cfg.getSearchServerConfigs()
            .getSearchServerConfigs().get(0).getFieldMappings().get(5)
            .getOperations().get(0).getTargets().get(3);
        Bsh bsh = Bsh.getInstance();
        bsh.passArguments(beanShellQuery.getTypeConfig(), helper.getXmlUtil());
        String r = bsh.getCode().replace(" ", "").replace("\n", "");
        Assert
            .assertEquals(
                "Xpath returned wrong value",
                "currentQuery=currentQuery.replace(\"XXX\",System.currentTimeMillis());",
                r);
    }

    @Test
    public void testMultiValue() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper
            .readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        List<FusionField> fl = cfg.getFusionFields().getFusionFields();
        Assert.assertNull("city shouldn't be a multi value field", findByName("city", fl).getMultiValue());
        Assert.assertTrue("city shouldn't be a multi value field", findByName("city", fl).isSingleValue());
        Assert.assertFalse("city shouldn't be a multi value field", findByName("city", fl).isMultiValue());
        Assert.assertEquals("multiValue1 shouldn't be a multi value field", Boolean.TRUE,
            findByName("multiValue1", fl).getMultiValue());
        Assert.assertFalse("multiValue1 shouldn't be a multi value field",
            findByName("multiValue1", fl).isSingleValue());
        Assert.assertTrue("multiValue1 shouldn't be a multi value field", findByName("multiValue1", fl).isMultiValue());
    }

    protected FusionField findByName(String name, List<FusionField> fl)
    {
        for (FusionField ff : fl)
        {
            if (ff.getFieldName().equals(name))
            {
                return ff;
            }
        }
        return null;
    }
}
