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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 6/19/14.
 */
@SuppressWarnings("unchecked")
public class ValueTest extends AbstractTypeTest
{

    @Test
    public void testSingleValue() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<value>abc123</value>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = Mockito.spy(new Value());
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(valueType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", Arrays.asList("abc123"), values);

        TypeResult opResult = valueType.apply(null, null, null, ConversionDirection.SEARCH_TO_FUSION);
        Assert.assertEquals("Found different values than expected", Arrays.asList("abc123"), opResult.getValues());
    }

    @Test
    public void testSeveralValues() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<value>a</value><value>b</value><value>c</value>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = Mockito.spy(new Value());
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(valueType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", Arrays.asList("a", "b", "c"), values);

        TypeResult opResult = valueType.apply(null, null, null, ConversionDirection.SEARCH_TO_FUSION);
        Assert.assertEquals("Found different values than expected", Arrays.asList("a", "b", "c"), opResult.getValues());
    }

    @Test
    public void testNoValue() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Value valueType = Mockito.spy(new Value());
        valueType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(valueType, Mockito.times(1)).logBadConfiguration(Mockito.eq(false), Mockito.anyList());

        List<String> values = valueType.getValues();
        Assert.assertEquals("Found different parsed value than expected", null, values);

        TypeResult opResult = valueType.apply(null, null, null, ConversionDirection.SEARCH_TO_FUSION);
        Assert.assertEquals("Found different values than expected", null, opResult.getValues());
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
        Term sourceField = buildResponseField(doc, "bibName", "dfskjfsdk7");

        ScriptEnv env = new ScriptEnv();
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", sourceField.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "source", sourceField.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("BIB-A"),
            sourceField.getFusionFieldValue());
    }

    @Test
    public void testQueryMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("source", "BIB-A");
        // query parser sets the fusionField automatically
        term.setFusionField(cfg.findFieldByName(term.getFusionFieldName()));
        Query query = new TermQuery(term);

        ScriptEnv env = new ScriptEnv();
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), query, env, null, QueryTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", term.isWasMapped());
        // System.out.println(term.toString());
        Assert.assertEquals("Found wrong field name mapping", "bibName", term.getSearchServerFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("bib1"), term.getSearchServerFieldValue());
    }
}
