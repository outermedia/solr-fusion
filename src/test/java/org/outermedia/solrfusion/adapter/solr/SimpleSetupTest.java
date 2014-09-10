package org.outermedia.solrfusion.adapter.solr;

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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.SolrServerDualTestBase;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by stephan on 03.06.14.
 */
public class SimpleSetupTest extends SolrServerDualTestBase {

    @Before
    public void fillSolr() throws IOException, SolrServerException {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", String.valueOf(1));
        document.addField("title", String.valueOf("Troilus und Cressida"));
        document.addField("author", String.valueOf("Shakespeare"));
        firstServer.add(document);
        firstTestServer.commitLastDocs();
    }

    @After
    public void cleanSolr() throws IOException, SolrServerException {
        firstServer.deleteByQuery("*:*");
    }

    @Test
    public void testMockAdapter() throws SolrServerException, SAXException, JAXBException, ParserConfigurationException, FileNotFoundException {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.addField("title");
        query.addField("author");
        query.addField("id");
        QueryResponse response = firstServer.query(query);
        InputStream inputStream = TestHelper.embeddedQueryToXmlInputStream(query, response);

        ResponseParserIfc responseParser = DefaultResponseParser.Factory.getInstance();
        XmlResponse xmlResponse = responseParser.parse(inputStream);

        Assert.assertEquals("Expected one document", 1, xmlResponse.getNumFound());
        String expected = "[Document(solrSingleValuedFields=[SolrSingleValuedField(super=SolrField(fieldName=id), value=1), SolrSingleValuedField(super=SolrField(fieldName=author), value=Shakespeare)], solrMultiValuedFields=[SolrMultiValuedField(super=SolrField(fieldName=title), values=[Troilus und Cressida])])]";
        Assert.assertEquals("Expected one document", expected, xmlResponse.getDocuments().toString());
    }
}