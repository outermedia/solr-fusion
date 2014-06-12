package org.outermedia.solrfusion;

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
