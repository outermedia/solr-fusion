package org.outermedia.solrfusion.types;

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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryTarget;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.parser.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ballmann on 6/19/14.
 */
@SuppressWarnings("unchecked")
public class TableTest extends AbstractTypeTest
{
    @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        String xml = docOpen + "<entry>\n" +
            "                   <value>u1</value>\n" +
            "                   <fusion-value>user1</fusion-value>\n" +
            "               </entry>\n" +
            "               <entry>\n" +
            "                   <value>u2</value>\n" +
            "                   <fusion-value>user2</fusion-value>\n" +
            "               </entry>\n" +
            "               <entry>\n" +
            "                   <value>u2</value>\n" +
            "                   <fusion-value>user3</fusion-value>\n" +
            "               </entry>\n" +
            docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        // System.out.println(util.xmlToString(elem));
        Table tableType = Mockito.spy(new Table());
        tableType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(tableType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        String fusion2search = tableType.getFusionToSearchServer().toString();
        String search2fusion = tableType.getSearchServerToFusion().toString();
        Assert.assertEquals("Parsing of configuration failed.", "{\n" +
            "\t,user2: [\"u2\"]\n" +
            "\t,user1: [\"u1\"]\n" +
            "\t,user3: [\"u2\"]\n" +
            "}", fusion2search);
        Assert.assertEquals("Parsing of configuration failed.", "{\n" +
            "\t,u2: [\"user3\", \"user2\"]\n" +
            "\t,u1: [\"user1\"]\n" +
            "}", search2fusion);
    }

    @Test
    public void testMissingConfig() throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        // System.out.println(util.xmlToString(elem));
        Table tableType = Mockito.spy(new Table());
        tableType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(tableType, Mockito.times(1)).logBadConfiguration(Mockito.eq(false), Mockito.anyList());

        Object fusion2search = tableType.getFusionToSearchServer();
        Object search2fusion = tableType.getSearchServerToFusion();
        Assert.assertNull("Handling of missing configuration failed.", fusion2search);
        Assert.assertNull("Handling of missing configuration failed.", search2fusion);
    }

    @Test
    public void testResponseMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        buildResponseField(doc, "Titel", "Ein kurzer Weg");
        buildResponseField(doc, "Autor", "Willi Schiller");
        buildResponseField(doc, "id", "132");
        Term sourceField = buildResponseField(doc, "f6", "u2", "u1");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null,
            ResponseTarget.ALL, true);
        Assert.assertTrue("Expected that term was mapped", sourceField.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "text2", sourceField.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("user2", "user1"),
            sourceField.getFusionFieldValue());
    }

    @Test
    public void testQueryMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("text2", "user1");
        // query parser sets the fusionField automatically
        term.setFusionField(cfg.findFieldByName(term.getFusionFieldName()));
        Query query = new TermQuery(term);

        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), query, env, null,
            QueryTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", term.isWasMapped());
        // System.out.println(term.toString());
        Assert.assertEquals("Found wrong field name mapping", "f6", term.getSearchServerFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("u1"), term.getSearchServerFieldValue());
    }
}
