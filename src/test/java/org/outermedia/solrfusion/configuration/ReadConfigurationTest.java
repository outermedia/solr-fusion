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

//		Assert.assertEquals("Found different configuration", expected,
//			config2Out);
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
}
