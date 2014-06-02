package org.outermedia.solrfusion.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.types.Bsh;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

public class ReadConfigurationTest
{
	protected Util xmlUtil;

	@Before
	public void setup()
	{
		xmlUtil = new Util();
	}

	@Test
	public void readFusionSchema() throws JAXBException, SAXException,
		ParserConfigurationException, IOException
	{
		// one xml file which contains servers too
		String config1 = "test-fusion-schema.xml";

		// with validation
		String schemaPath = "configuration.xsd";

		Configuration cfg1 = xmlUtil.unmarshal(Configuration.class, config1,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config1, cfg1);

		String config1Out = addNewlines(cfg1.toString());
		// System.out.println("CONFIG1 " + config1Out);

		// this configuration uses <xi:include> to include server declarations
		String config2 = "test-global-fusion-schema.xml";
		Configuration cfg2 = xmlUtil.unmarshal(Configuration.class, config2,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config2, cfg2);

		String config2Out = addNewlines(cfg2.toString());
		// System.out.println("CONFIG2\n" + config2Out);

		Assert.assertEquals(
			"<xi:include> should work transparently, but differences occurred",
			addNewlines(config1Out), (config2Out));

		String expected = Files
			.toString(new File("src/test/resources/schema-toString.txt"),
				Charset.forName("UTF-8"));

		expected = addNewlines(expected);

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
		// one xml file which contains servers too
		String config = "test-fusion-schema.xml";

		// with validation
		String schemaPath = "configuration.xsd";

		Configuration cfg = xmlUtil.unmarshal(Configuration.class, config,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config, cfg);

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
		bsh.passArguments(beanShellQuery.getTypeConfig(), xmlUtil);
		String r = bsh.getCode().replace(" ", "").replace("\n", "");
		Assert
			.assertEquals(
				"Xpath returned wrong value",
				"currentQuery=currentQuery.replace(\"XXX\",System.currentTimeMillis());",
				r);
	}
}
