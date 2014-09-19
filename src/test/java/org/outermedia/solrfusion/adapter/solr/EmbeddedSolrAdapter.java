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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.Multimap;
import org.outermedia.solrfusion.SolrTestServer;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.AdapterConfig;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.StringTokenizer;

import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author stephan
 */
@ToString
@Slf4j
public class EmbeddedSolrAdapter implements SearchServerAdapterIfc<SolrFusionSolrQuery>
{

    protected String WRITER_TYPE_PARAMETER = "wt";

    private SolrTestServer testServer;

    @Getter @Setter String url;

    /**
     * Factory creates instances only.
     */
    private EmbeddedSolrAdapter()
    {
    }

    @Override
    public SolrFusionSolrQuery buildHttpClientParams(Configuration configuration, SearchServerConfig searchServerConfig,
        FusionRequest fusionRequest, Multimap<String> params, String version) throws URISyntaxException
    {
        String q = params.getFirst(SolrFusionRequestParams.QUERY);
        String fq = params.getFirst(SolrFusionRequestParams.FILTER_QUERY);
        SolrFusionSolrQuery query = new SolrFusionSolrQuery(q);
        if (fq != null)
        {
            query.setFilterQueries(fq);
        }
        String responseFormat = params.getFirst(WRITER_TYPE);
        query.set(WRITER_TYPE_PARAMETER, responseFormat);
        String start = params.getFirst(START);
        if (start == null)
        {
            start = "0";
        }
        query.setStart(Integer.valueOf(start));
        String rows = params.getFirst(PAGE_SIZE);
        if (rows == null)
        {
            rows = "30";
        }
        query.setRows(Integer.valueOf(rows));
        String sortStr = params.getFirst(SORT);
        if (sortStr == null)
        {
            sortStr = "score desc";
        }
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        String sortField = st.nextToken();
        String sortDir = st.nextToken();
        query.setSort(sortField, sortDir.equals("asc") ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        String fieldsToReturn = params.getFirst(FIELDS_TO_RETURN);
        fieldsToReturn = mergeField(sortField, fieldsToReturn);
        String fieldsToReturnArr[] = fieldsToReturn.split(" ");
        query.setFields(fieldsToReturnArr);
        String queryType = params.getFirst(QUERY_TYPE);
        if (queryType != null)
        {
            query.setRequestHandler(queryType);
        }

        log.debug("Prepared query for server {}: q={} fq={} start={} rows={} sort={} fl={}",
            searchServerConfig.getSearchServerName(), q, fq, start, rows, sortStr, fieldsToReturn);

        return query;
    }

    @Override
    public InputStream sendQuery(SolrFusionSolrQuery query, int timeout) throws URISyntaxException, IOException
    {
        QueryResponse response = null;
        try
        {
            response = testServer.query(query);
        }
        catch (SolrServerException e)
        {
            log.error("Caught exception during solr send/receive", e);
        }
        InputStream inputStream = TestHelper.embeddedQueryToXmlInputStream(query, response);

        return inputStream;
    }

    /**
     * Add a field to a field list if it is not already contained.
     *
     * @param field
     * @param fieldList are separated by SPACE (see {@link org.outermedia.solrfusion.FusionRequest#mapFusionFieldListToSearchServerField(String,
     *                  org.outermedia.solrfusion.configuration.Configuration, org.outermedia.solrfusion.configuration.SearchServerConfig,
     *                  String, boolean, org.outermedia.solrfusion.configuration.QueryTarget)} )
     * @return a modified field list
     */
    protected String mergeField(String field, String fieldList)
    {
        if ((" " + fieldList + " ").contains(" " + field + " "))
        {
            return fieldList;
        }
        return fieldList + " " + field;
    }

    public static class Factory
    {
        public static EmbeddedSolrAdapter getInstance()
        {
            return new EmbeddedSolrAdapter();
        }
    }

    @Override
    public void init(SearchServerConfig config)
    {
        url = config.getUrl();
        AdapterConfig adapterConfigObj = config.getAdapterConfig();
        List<Element> adapterConfigXml = adapterConfigObj.getTypeConfig();
        try
        {
            /* unfortunately the ":" is necessary for the empty xml namespace!
             * please see Util.getValueOfXpath() */
            String solrHome = new Util().getValueOfXpath("//:solr-home", adapterConfigXml, true);
            initTestServer(solrHome);
        }
        catch (Exception e)
        {
            log.error("Caught exception while creating embedded solr server", e);
        }
    }

    public void initTestServer(String solrHome) throws Exception
    {
        testServer = new SolrTestServer(solrHome);
    }

    @Override public void finish() throws Exception
    {
        testServer.finish();
    }

    public void commitLastDocs()
    {
        testServer.commitLastDocs();
    }

    @Override public void add(Document doc) throws Exception
    {
        final SolrInputDocument document = new SolrInputDocument();
        doc.accept(new FieldVisitor()
        {
            @Override public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
            {
                document.addField(sf.getFieldName(), sf.getValue());
                return true;
            }

            @Override public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
            {
                List<String> values = msf.getValues();
                for(String v : values) document.addField(msf.getFieldName(), v);
                return true;
            }
        }, new ScriptEnv());
        testServer.add(document);
    }

    @Override public void deleteByQuery(String query) throws Exception
    {
        testServer.deleteByQuery(query);
    }

}

