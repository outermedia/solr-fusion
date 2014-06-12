package org.outermedia.solrfusion.response;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.XMLResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses a solr server's xml response into an internal representation.
 * 
 * @author ballmann
 * 
 */

@ToString
public class DefaultResponseParser implements ResponseParserIfc
{
    private Util xmlUtil;

	/**
	 * Factory creates instances only.
	 */
	private DefaultResponseParser()
	{}

    @Override
    public XMLResponse parse(InputStream input) throws ParserConfigurationException, FileNotFoundException, JAXBException, SAXException {

        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        XMLResponse response = xmlUtil.unmarshal(XMLResponse.class, "", br, null);
        return response;
    }

    public static class Factory
	{
		public static DefaultResponseParser getInstance()
		{
			return new DefaultResponseParser();
		}
	}

	@Override
	public void init(ResponseParserFactory config)
	{
		// TODO Auto-generated method stub
        xmlUtil = new Util();
	}

}
