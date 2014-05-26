package org.outermedia.solrfusion.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ReadConfigurationTest
{
	@Test
	public void readFusionSchema() throws JAXBException, SAXException,
		FileNotFoundException
	{
		Util xmlUtil = new Util();
		String config = "test-fusion-schema.xml";
		// no validation
		String schemaPath = null;
		Configuration cfg = xmlUtil.unmarshal(Configuration.class, config,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config, cfg);
		System.out.println(cfg.toString());
	}
}
