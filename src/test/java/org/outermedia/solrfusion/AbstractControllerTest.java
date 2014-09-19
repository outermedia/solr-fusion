package org.outermedia.solrfusion;

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

import com.google.common.io.Files;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.adapter.SolrFusionUriBuilderIfc;
import org.outermedia.solrfusion.adapter.solr.SolrFusionUriBuilder;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Merge;
import org.outermedia.solrfusion.configuration.ResponseRendererType;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * Created by ballmann on 8/6/14.
 */
public class AbstractControllerTest
{
    ByteArrayInputStream testResponse;
    TestHelper helper;
    @Mock ResponseRendererIfc testRenderer;
    @Mock SearchServerAdapterIfc testAdapter;
    Configuration cfg;
    @Mock SearchServerAdapterIfc testAdapter9000;

    @Mock SearchServerAdapterIfc testAdapter9002;

    @Mock
    private SearchServerConfig testSearchConfig;

    @Mock
    private SolrFusionUriBuilderIfc testParams;

    protected Multimap<String> buildParams(String q, String fq)
    {
        Multimap<String> result = new Multimap<>();
        result.put(QUERY, q);
        if (fq != null)
        {
            result.put(FILTER_QUERY, fq);
        }
        result.put(FIELDS_TO_RETURN, "* score id");
        result.put(WRITER_TYPE, "xml");
        result.put(FILTER_QUERY, "title:newFQ");
        return result;
    }

    static class TestMerger extends Merge
    {
        public void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
        {
            super.afterUnmarshal(u, parent);
        }
    }

    @Before
    public void setup() throws IOException, ParserConfigurationException, JAXBException, SAXException
    {
        helper = new TestHelper();
        MockitoAnnotations.initMocks(this);
        cfg = null;
        initTestResponse();
    }

    protected void initTestResponse() throws IOException
    {
        byte[] emptyResponseBytes = Files.toByteArray(new File("target/test-classes/test-empty-xml-response.xml"));
        testResponse = new ByteArrayInputStream(emptyResponseBytes);
    }

    protected FusionControllerIfc createTestFusionController(String fusionSchema)
        throws IOException, JAXBException, SAXException, ParserConfigurationException, InvocationTargetException,
        IllegalAccessException, URISyntaxException
    {
        cfg = spy(helper.readFusionSchemaWithoutValidation(fusionSchema));
        when(testRenderer.getResponseString(any(Configuration.class), any(ClosableIterator.class),
            any(FusionRequest.class), any(FusionResponse.class))).thenReturn("<xml>42</xml>");
        when(cfg.getResponseRendererByType(any(ResponseRendererType.class))).thenReturn(testRenderer);
        List<SearchServerConfig> searchServerConfigs = cfg.getSearchServerConfigs().getSearchServerConfigs();
        if (searchServerConfigs != null && !searchServerConfigs.isEmpty())
        {
            SearchServerConfig searchServerConfig = spy(searchServerConfigs.get(0));
            searchServerConfigs.clear();
            searchServerConfigs.add(searchServerConfig);
            when(searchServerConfig.getInstance()).thenReturn(testAdapter);
            when(testAdapter.buildHttpClientParams(any(Configuration.class), any(SearchServerConfig.class),
                any(FusionRequest.class), any(Multimap.class), anyString())).thenReturn(testParams);
            when(testAdapter.sendQuery(any(SolrFusionUriBuilder.class), Mockito.anyInt())).thenReturn(testResponse);
        }
        return cfg.getController();
    }
}
