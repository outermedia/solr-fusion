package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.XmlResponse;
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
 */

@ToString
@Slf4j
public class DefaultResponseParser implements ResponseParserIfc
{
    private Util xmlUtil;

    /**
     * Factory creates instances only.
     */
    protected DefaultResponseParser()
    {
        xmlUtil = new Util();
    }

    @Override
    public XmlResponse parse(InputStream input)
            throws ParserConfigurationException, FileNotFoundException, JAXBException, SAXException
    {

        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        XmlResponse response = xmlUtil.unmarshal(XmlResponse.class, "", br, null);
        if (response != null)
        {
            if (response.getDocuments() != null)
            {
                log.debug("Query returned {} documents.", response.getDocuments().size());
            }
            else
            {
                log.debug("Query returned no documents at all.");
            }
        }
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
    }

}
