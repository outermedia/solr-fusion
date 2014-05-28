package org.outermedia.solrfusion.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Helper class which simplifies the reading of XML files.
 * 
 * @author ballmann
 * 
 */

@Slf4j
public class Util
{
	/**
	 * Read xml into an object of class docClass.
	 * 
	 * @param docClass objects of this class represent the xml
	 * @param xml the resource path to an XML file
	 * @param schemaPath null (no validation) or a resource path to an XML
	 *            schema file.
	 * @return null for error or an instance of class docClass.
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 */
	public <T> T unmarshal(Class<T> docClass, String xml, String schemaPath)
		throws JAXBException, SAXException, FileNotFoundException,
		ParserConfigurationException
	{
		String xmlPath = findXmlInClasspath(xml);
		T result = null;
		if (xmlPath != null)
		{
			Reader xmlReader = new FileReader(new File(xmlPath));
			result = (T) unmarshal(docClass, xml, xmlReader, schemaPath);
		}
		return result;
	}

	/**
	 * Read xml from reader into an object of class docClass.
	 * 
	 * @param docClass objects of this class represent the xml
	 * @param xmlPath the resource path whose content is returned by xmlReader
	 *            (only used in log statement)
	 * @param xmlReader the xml to read in
	 * @param schemaPath null (no validation) or a resource path to an XML
	 *            schema file.
	 * @return null for error or an instance of class docClass.
	 * @throws JAXBException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(Class<T> docClass, String xmlPath, Reader xmlReader,
		String schemaPath) throws JAXBException, SAXException,
		ParserConfigurationException
	{
		JAXBContext jc = JAXBContext.newInstance(new Class[]
		{
			docClass
		});
		Unmarshaller u = jc.createUnmarshaller();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setXIncludeAware(true);
		spf.setNamespaceAware(true);
		URL schemaUrl = null;
		XmlValidationHandler validationHandler = null;
		if (schemaPath != null)
		{
			schemaUrl = Util.class.getResource("/" + schemaPath);
			if (schemaUrl != null)
			{
				SchemaFactory sf = SchemaFactory
					.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(schemaUrl);
				u.setSchema(schema);
				validationHandler = new XmlValidationHandler();
				u.setEventHandler(validationHandler);
			}
			else
			{
				log.error("Can't find resource '/{}'. Can't validate.",
					schemaPath);
			}
		}
		log.info("{} Reading conf file: '{}' (schema: '{}' -> {})",
			docClass.getName(), xmlPath, schemaPath, schemaUrl);

		XMLReader xr = spf.newSAXParser().getXMLReader();
		// prevent validation error: 
		// Attribute 'xml:base' is not allowed to appear in element 'om:solr-server'. at line=78, column=119
		// when <xi:include> is used
		xr.setFeature(
			"http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		SAXSource source = new SAXSource(xr, new InputSource(xmlReader));
		T result = (T) u.unmarshal(source);
		if (validationHandler != null && validationHandler.isFoundErrors())
		{
			log.warn("Discarding unmarshalled xml result, because of validation errors.");
			result = null;
		}
		return result;
	}

	/**
	 * Get the path to a configuration file which is located by its resource
	 * path.
	 * @param resourcePath
	 * @return null for error or a path to the resource.
	 */
	public String findXmlInClasspath(String resourcePath)
	{
		String result = null;
		URL url = Util.class.getClassLoader().getResource(resourcePath);
		if (url == null)
		{
			log.error("Didn't find '{}' in classpath", resourcePath);
		}
		else
		{
			result = url.getFile();
		}
		return result;
	}

}
