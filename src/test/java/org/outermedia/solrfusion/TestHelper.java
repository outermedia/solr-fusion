package org.outermedia.solrfusion;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import lombok.Getter;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Util;
import org.xml.sax.SAXException;

@Getter
public class TestHelper
{
	protected Util xmlUtil;

	public TestHelper()
	{
		xmlUtil = new Util();
	}

	public Configuration readFusionSchemaWithoutValidation(String xmlPath)
		throws FileNotFoundException, JAXBException, SAXException,
		ParserConfigurationException
	{
		return readFusionSchemaWithValidation(xmlPath, null);
	}

	public Configuration readFusionSchemaWithValidation(String xmlPath,
		String schemaPath) throws FileNotFoundException, JAXBException,
		SAXException, ParserConfigurationException
	{
		Configuration cfg = xmlUtil.unmarshal(Configuration.class, xmlPath,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ xmlPath, cfg);
		return cfg;
	}
}
