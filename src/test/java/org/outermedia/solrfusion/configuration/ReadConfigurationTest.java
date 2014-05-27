package org.outermedia.solrfusion.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

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
		FileNotFoundException, ParserConfigurationException
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

		String config1Out = cfg1.toString();
		// System.out.println("CONFIG1 " + config1Out);

		// this configuration uses <xi:include> to include server declarations
		String config2 = "test-global-fusion-schema.xml";
		Configuration cfg2 = xmlUtil.unmarshal(Configuration.class, config2,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config2, cfg2);

		String config2Out = cfg2.toString();
		// System.out.println("CONFIG2 " + config2Out);

		Assert.assertEquals(
			"<xi:include> should work transparently, but differences occurred",
			config1Out, config2Out);

	}
}
