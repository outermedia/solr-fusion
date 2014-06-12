package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseParserFactory;
import org.outermedia.solrfusion.response.parser.XMLResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Transforms a search server's response into an internal representation.
 * 
 * @author ballmann
 * 
 */
public interface ResponseParserIfc extends Initiable<ResponseParserFactory>
{
    // TODO: it may make sense to define a ResponseIfc for return Value
    public abstract XMLResponse parse(InputStream input) throws SAXException, ParserConfigurationException, FileNotFoundException, JAXBException;
}
