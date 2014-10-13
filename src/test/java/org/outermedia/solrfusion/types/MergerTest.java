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
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.ResponseMapper;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ballmann on 6/19/14.
 */
public class MergerTest extends AbstractTypeTest
{
    @SuppressWarnings("unchecked") @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<range>1</range><separator> </separator>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        MultiValueMerger mergeType = Mockito.spy(new MultiValueMerger());
        mergeType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(mergeType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        String range = mergeType.getRange();
        String separator = mergeType.getSeparator();
        Assert.assertEquals("Parsing of configuration failed.", "1", range);
        Assert.assertEquals("Parsing of configuration failed.", " ", separator);
    }

    @SuppressWarnings("unchecked") @Test
    public void testMissingConfig() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Js jsType = Mockito.spy(new Js());
        jsType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(jsType, Mockito.times(1)).logBadConfiguration(Mockito.eq(false), Mockito.anyList());

        String code = jsType.getCode();
        Assert.assertNull("Handling of missing configuration failed.", code);
    }

    @Test
    public void testResponseMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        ResponseMapperIfc rm = ResponseMapper.Factory.getInstance();
        Document doc = buildResponseDocument();
        Term titleTerm = buildResponseField(doc, "s6", "Ein kurzer Weg", "A short way", "A very long way");
        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL, true);
        Assert.assertTrue("Expected that term was mapped", titleTerm.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "text6", titleTerm.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("Ein kurzer Weg"),
            titleTerm.getFusionFieldValue());

        doc = buildResponseDocument();
        titleTerm = buildResponseField(doc, "s7", "Ein kurzer Weg", "A short way", "A very long way");
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL, true);
        Assert.assertTrue("Expected that term was mapped", titleTerm.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "text7", titleTerm.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("Ein kurzer Weg,A short way"),
            titleTerm.getFusionFieldValue());

        doc = buildResponseDocument();
        titleTerm = buildResponseField(doc, "s8", "Ein kurzer Weg", "A short way", "A very long way");
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL, true);
        Assert.assertTrue("Expected that term was mapped", titleTerm.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "text8", titleTerm.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping",
            Arrays.asList("Ein kurzer Weg || A short way || A very long way"), titleTerm.getFusionFieldValue());
    }

}
