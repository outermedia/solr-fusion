package org.outermedia.solrfusion.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

public class ResponseRendererTest
{

	protected TestHelper helper;

	@Before
	public void setup()
	{
		helper = new TestHelper();
	}

	@Test
	public void findCertainRenderer() throws FileNotFoundException,
		JAXBException, SAXException, ParserConfigurationException
	{
		Configuration cfg = helper
			.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

		ResponseRendererIfc xmlRenderer = cfg
			.getResponseRendererByType(ResponseRendererType.XML);
		Assert.assertNotNull("Expected to find an xml renderer", xmlRenderer);

		ResponseRendererIfc jsonRenderer = cfg
			.getResponseRendererByType(ResponseRendererType.JSON);
		Assert.assertNotNull("Expected to find a json renderer", jsonRenderer);

		ResponseRendererIfc phpRenderer = cfg
			.getResponseRendererByType(ResponseRendererType.PHP);
		Assert.assertNotNull("Expected to find a php renderer", phpRenderer);
	}
}
