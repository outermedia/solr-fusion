package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Transforms a Solr search server's response into an internal representation.
 *
 * @author ballmann
 */
public interface ResponseParserIfc extends Initiable<ResponseParserFactory>
{
    /**
     * Parse a Solr response from the given input.
     *
     * @param input contains a complete Solr response
     * @return perhaps null
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    public abstract XmlResponse parse(InputStream input)
        throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException;
}
