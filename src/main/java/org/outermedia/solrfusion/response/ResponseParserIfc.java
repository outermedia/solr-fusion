package org.outermedia.solrfusion.response;

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
