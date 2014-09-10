package org.outermedia.solrfusion;

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

import lombok.ToString;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Dummy implementation for unit test.
 * 
 * @author ballmann
 * 
 */

@ToString
public class SpecialResponseParser implements ResponseParserIfc
{
	private SpecialResponseParser()
	{}

    @Override
    public XmlResponse parse(InputStream input) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException {
        return null;
    }

    public static class Factory
	{
		public static Object getInstance()
		{
			return new SpecialResponseParser();
		}
	}

	@Override
	public void init(ResponseParserFactory config)
	{
		// NOP
	}


}
