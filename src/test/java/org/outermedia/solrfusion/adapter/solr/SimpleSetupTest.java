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
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.SolrServerDualTestBase;
import org.outermedia.solrfusion.response.DefaultResponseParser;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by stephan on 03.06.14.
 */
public class SimpleSetupTest extends SolrServerDualTestBase {

    private EmbeddedSolrAdapter adapter;

    @Before
    public void setup() throws Exception
    {
        adapter = EmbeddedSolrAdapter.Factory.getInstance();
        adapter.initTestServer("src/test/resources/solr/solr-home-1");

        Document document = new Document();
        document.addField("id", String.valueOf(1));
        document.addField("title", String.valueOf("Troilus und Cressida"));
        document.addField("author", String.valueOf("Shakespeare"));
        adapter.add(document);
        adapter.commitLastDocs();
    }

    @After
    public void cleanSolr() throws Exception
    {
        adapter.deleteByQuery("*:*");
        adapter.finish();
    }

    @Test
    public void testMockAdapter()
        throws SolrServerException, SAXException, JAXBException, ParserConfigurationException, IOException,
        URISyntaxException
    {
        SolrFusionSolrQuery query = new SolrFusionSolrQuery("*:*");
        query.setRows(Integer.MAX_VALUE);
        query.addField("title");
        query.addField("author");
        query.addField("id");
        InputStream inputStream = adapter.sendQuery(query, 2000);

        ResponseParserIfc responseParser = DefaultResponseParser.Factory.getInstance();
        XmlResponse xmlResponse = responseParser.parse(inputStream);

        Assert.assertEquals("Expected one document", 1, xmlResponse.getNumFound());
        String expected = "[Document(solrSingleValuedFields=[SolrSingleValuedField(super=SolrField(fieldName=id), value=1), SolrSingleValuedField(super=SolrField(fieldName=author), value=Shakespeare)], solrMultiValuedFields=[SolrMultiValuedField(super=SolrField(fieldName=title), values=[Troilus und Cressida])])]";
        Assert.assertEquals("Expected one document", expected, xmlResponse.getDocuments().toString());
    }
}