package org.outermedia.solrfusion.configuration;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

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
            JAXBException, SAXException, ParserConfigurationException, InvocationTargetException, IllegalAccessException
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
