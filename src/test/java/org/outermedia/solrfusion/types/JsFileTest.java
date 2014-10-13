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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Created by ballmann on 6/19/14.
 */
@SuppressWarnings("unechecked")
public class JsFileTest extends AbstractTypeTest
{
    @SuppressWarnings("unchecked")
    @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<file>target/test-classes/test-js-file.js</file>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        JsFile jsType = spy(new JsFile());
        jsType.passArguments(util.filterElements(elem.getChildNodes()), util);
        verify(jsType, times(1)).logBadConfiguration(eq(true), anyList());

        String code = jsType.getCode();
        String expected = FileUtils.readFileToString(new File("target/test-classes/test-js-file.js"));
        Assert.assertEquals("Parsing of configuration failed.", expected, code);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMissingConfig() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        JsFile jsType = spy(new JsFile());
        jsType.passArguments(util.filterElements(elem.getChildNodes()), util);
        verify(jsType, times(1)).logBadConfiguration(eq(false), anyList());

        String code = jsType.getCode();
        Assert.assertNull("Handling of missing configuration failed.", code);
    }

    @Test
    public void testResponseMapping() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();

        buildResponseField(doc, "Titel", "Ein kurzer Weg");
        buildResponseField(doc, "Autor", "Willi Schiller");
        buildResponseField(doc, "id", "132");
        Term sourceField = buildResponseField(doc, "f4", "something3");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL, true);
        Assert.assertTrue("Expected that term was mapped", sourceField.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "today4", sourceField.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("something3 at 2014-07-19"),
                sourceField.getFusionFieldValue());
    }

    @Test
    public void testQueryMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("today4", "something");
        // query parser sets the fusionField automatically
        term.setFusionField(cfg.findFieldByName(term.getFusionFieldName()));
        Query query = new TermQuery(term);

        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), query, env, null, QueryTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", term.isWasMapped());
        // System.out.println(term.toString());
        Assert.assertEquals("Found wrong field name mapping", "f4", term.getSearchServerFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("something at 2014-07-19"),
                term.getSearchServerFieldValue());
    }
}
