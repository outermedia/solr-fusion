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
import org.outermedia.solrfusion.FusionRequest;
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

/**
 * Created by ballmann on 6/19/14.
 */
public class BshTest extends AbstractTypeTest
{
    @SuppressWarnings("unchecked")
    @Test
    public void testConfigParsing() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + "<script>return 42;</script>" + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Bsh bshType = Mockito.spy(new Bsh());
        bshType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(bshType, Mockito.times(1)).logBadConfiguration(Mockito.eq(true), Mockito.anyList());

        String code = bshType.getCode();
        Assert.assertEquals("Parsing of configuration failed.", "return 42;", code);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMissingConfig() throws IOException, SAXException, ParserConfigurationException
    {
        String xml = docOpen + docClose;

        Util util = new Util();
        Element elem = util.parseXml(xml);
        Bsh bshType = Mockito.spy(new Bsh());
        bshType.passArguments(util.filterElements(elem.getChildNodes()), util);
        Mockito.verify(bshType, Mockito.times(1)).logBadConfiguration(Mockito.eq(false), Mockito.anyList());

        String code = bshType.getCode();
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
        Term sourceField = buildResponseField(doc, "f1", "something", "other");

        ScriptEnv env = new ScriptEnv();
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
        env.setBinding(ScriptEnv.ENV_IN_MAP_FACET, true);
        env.setBinding(ScriptEnv.ENV_IN_MAP_HIGHLIGHT, true);
        rm.mapResponse(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), doc, env, null, ResponseTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", sourceField.isWasMapped());
        // System.out.println(sourceField.toString());
        Assert.assertEquals("Found wrong field name mapping", "today", sourceField.getFusionFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("something at 2014-07-19"),
                sourceField.getFusionFieldValue());
        Assert.assertEquals("Found wrong facet word counts", Arrays.asList("4", "1", "3", "2"),
            sourceField.getFusionFacetCount());
    }

    @Test
    public void testQueryMapping()
            throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        QueryMapperIfc qm = QueryMapper.Factory.getInstance();
        Term term = Term.newFusionTerm("today", "something");
        // query parser sets the fusionField automatically
        term.setFusionField(cfg.findFieldByName(term.getFusionFieldName()));
        Query query = new TermQuery(term);

        ScriptEnv env = new ScriptEnv();
        env.setBinding(ScriptEnv.ENV_IN_FUSION_REQUEST, new FusionRequest());
        env.setBinding(ScriptEnv.ENV_IN_MAP_FACET, true);
        env.setBinding(ScriptEnv.ENV_IN_MAP_HIGHLIGHT, true);
        qm.mapQuery(cfg, cfg.getSearchServerConfigs().getSearchServerConfigs().get(0), query, env, null, QueryTarget.ALL);
        Assert.assertTrue("Expected that term was mapped", term.isWasMapped());
        // System.out.println(term.toString());
        Assert.assertEquals("Found wrong field name mapping", "f1", term.getSearchServerFieldName());
        Assert.assertEquals("Found wrong field value mapping", Arrays.asList("something at 2014-07-19"),
                term.getSearchServerFieldValue());
    }
}
