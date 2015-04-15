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

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Helper class which simplifies the reading of XML files.
 *
 * @author ballmann
 */

@Slf4j
public class Util
{
    /**
     * Read xml into an object of class docClass.
     *
     * @param docClass
     *     objects of this class represent the xml
     * @param xml
     *     the resource path to an XML file
     * @param schemaPath
     *     null (no validation) or a resource path to an XML schema file.
     * @return null for error or an instance of class docClass.
     * @throws JAXBException
     * @throws SAXException
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     */
    public <T> T unmarshal(Class<T> docClass, String xml, String schemaPath)
        throws JAXBException, SAXException, FileNotFoundException, ParserConfigurationException
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
     * @param docClass
     *     objects of this class represent the xml
     * @param xmlPath
     *     the resource path whose content is returned by xmlReader (only used in log statement)
     * @param xmlReader
     *     the xml to read in
     * @param schemaPath
     *     null (no validation) or a resource path to an XML schema file.
     * @return null for error or an instance of class docClass.
     * @throws JAXBException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @SuppressWarnings("unchecked")
    public <T> T unmarshal(Class<T> docClass, String xmlPath, Reader xmlReader, String schemaPath)
        throws JAXBException, SAXException, ParserConfigurationException
    {
        JAXBContext jc = JAXBContext.newInstance(new Class[]{docClass});
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
                SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = sf.newSchema(schemaUrl);
                u.setSchema(schema);
                validationHandler = new XmlValidationHandler();
                u.setEventHandler(validationHandler);
            }
            else
            {
                log.error("Can't find resource '/{}'. Can't validate.", schemaPath);
            }
        }
        log.info("{} Reading conf file: '{}' (schema: '{}' -> {})", docClass.getName(), xmlPath, schemaPath, schemaUrl);

        XMLReader xr = spf.newSAXParser().getXMLReader();
        // prevent validation error:
        // Attribute 'xml:base' is not allowed to appear in element 'om:solr-server'. at line=78, column=119
        // when <xi:include> is used
        xr.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        SAXSource source = new SAXSource(xr, new InputSource(xmlReader));
        T result = (T) u.unmarshal(source);
        if (validationHandler != null && validationHandler.isFoundErrors())
        {
            log.warn("Discarding unmarshalled xml result, because of validation errors.");
            result = null;
        }
        return result;
    }

    public <T> T unmarshal(Class<T> docClass, Node node) throws JAXBException
    {
        JAXBContext jc = JAXBContext.newInstance(new Class[]{docClass});
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> jaxb = u.unmarshal(node, docClass);
        T result = null;
        if (jaxb != null)
        {
            result = jaxb.getValue();
        }
        return result;
    }

    /**
     * Get the path to a configuration file which is located by its resource path.
     *
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
            log.info("Using '" + resourcePath + "' in: " + result);
        }
        return result;
    }

    /**
     * Get the value for a given xpath and a list of elements. VERY IMPORTANT: Use the ":" e.g. in //:script, otherwise
     * no xpath will match. The value is trimmed.
     *
     * @param xpathStr
     *     is the xpath.
     * @param typeConfig
     *     is a list of dom w3c elements
     * @return null if nothing is found or the value of the first(!) matched element
     * @throws XPathExpressionException
     */
    public String getValueOfXpath(String xpathStr, List<Element> typeConfig) throws XPathExpressionException
    {
        return getValueOfXpath(xpathStr, typeConfig, true);
    }

    /**
     * Get the value for a given xpath and a list of elements. VERY IMPORTANT: Use the ":" e.g. in //:script, otherwise
     * no xpath will match. The value is trimmed if the trim parameter is true.
     *
     * @param xpathStr
     *     is the xpath.
     * @param typeConfig
     *     is a list of dom w3c elements
     * @param trim
     *     whether to trim the found value or not
     * @return null if nothing is found or the value of the first(!) matched element
     * @throws XPathExpressionException
     */
    public String getValueOfXpath(String xpathStr, List<Element> typeConfig, boolean trim)
        throws XPathExpressionException
    {
        String result = null;
        List<Node> nl = xpath(xpathStr, typeConfig);
        if (nl.size() > 0)
        {
            result = nl.get(0).getTextContent();
        }
        if (result != null && trim)
        {
            result = result.trim();
        }
        return result;
    }

    /**
     * Get a elements, matching the given xpath. VERY IMPORTANT: Use the ":" e.g. in //:script, otherwise no xpath will
     * match.
     *
     * @param xpathStr
     *     is the xpath
     * @param typeConfig
     *     is a list of dom w3c elements
     * @return a list of w3c org nodes (perhaps empty)
     * @throws XPathExpressionException
     */
    public List<Node> xpath(String xpathStr, List<Element> typeConfig) throws XPathExpressionException
    {
        List<Node> result = new ArrayList<>();
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        setNameSpaceForXpath(xpath);
        XPathExpression expr = xpath.compile(xpathStr);
        Map<Node, Boolean> alreadyFound = new HashMap<>();
        for (Element e : typeConfig)
        {
            Object r = expr.evaluate(e, XPathConstants.NODESET);
            NodeList nl = (NodeList) r;
            if (nl != null && nl.getLength() > 0)
            {
                for (int i = 0; i < nl.getLength(); i++)
                {
                    // if the typeConfig elements are siblings, then evaluate() returns the siblings too!
                    // so it is necessary to check whether a node was already added or not
                    if (!alreadyFound.containsKey(nl.item(i)))
                    {
                        result.add(nl.item(i));
                        alreadyFound.put(nl.item(i), Boolean.TRUE);
                    }
                }
            }
        }
        return result;
    }

    public boolean xpathPresent(String xpathStr, List<Element> typeConfig) throws XPathExpressionException
    {
        boolean result = false;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        setNameSpaceForXpath(xpath);
        XPathExpression expr = xpath.compile(xpathStr);
        for (Element e : typeConfig)
        {
            Object r = expr.evaluate(e, XPathConstants.NODESET);
            NodeList nl = (NodeList) r;
            if (nl != null && nl.getLength() > 0)
            {
                result = true;
                break;
            }
        }
        return result;
    }

    protected void setNameSpaceForXpath(XPath xpath)
    {
        xpath.setNamespaceContext(new NamespaceContext()
        {
            public String getNamespaceURI(String prefix)
            {
                // only the default name space is possible
                return "http://solrfusion.outermedia.org/configuration/type/";
            }

            public String getPrefix(String uri)
            {
                throw new UnsupportedOperationException();
            }

            public Iterator<?> getPrefixes(String uri)
            {
                throw new UnsupportedOperationException();
            }
        });
    }

    public List<Element> xpathElements(String xpathStr, List<Element> typeConfig) throws XPathExpressionException
    {
        List<Node> nodes = xpath(xpathStr, typeConfig);
        return filterElements(nodes);
    }

    public List<Element> filterElements(List<Node> nodes)
    {
        List<Element> result = new ArrayList<>();
        for (Node n : nodes)
        {
            if (n instanceof Element)
            {
                result.add((Element) n);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Element> filterElements(NodeList nodes)
    {
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node n = nodes.item(i);
            if (n instanceof Element)
            {
                result.add((Element) n);
            }
        }
        return result;
    }

    public Element parseXml(String xml) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
        return doc.getDocumentElement();
    }

    public String xmlToString(Node n) throws TransformerException
    {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(n), new StreamResult(buffer));
        return buffer.toString();
    }

    public Element parseXmlFromFile(String file) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(file));
        return doc.getDocumentElement();
    }
}
