package org.outermedia.solrfusion.adapter.solr;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.outermedia.solrfusion.SolrTestServer;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringTokenizer;

import static org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter.*;
import static org.outermedia.solrfusion.query.SolrFusionRequestParams.*;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author stephan
 */
@ToString
@Slf4j
public class EmbeddedSolrAdapter implements SearchServerAdapterIfc
{

    @Setter @Getter private SolrTestServer testServer;

    String corePath;

    /**
     * Factory creates instances only.
     */
    private EmbeddedSolrAdapter()
    {
    }

    @Override
    public InputStream sendQuery(Map<String, String> params, int timeout) throws URISyntaxException, IOException
    {
        String q = params.get(SolrFusionRequestParams.QUERY.getRequestParamName());
        String fq = params.get(SolrFusionRequestParams.FILTER_QUERY.getRequestParamName());
        SolrQuery query = new SolrQuery(q);
        if (fq != null)
        {
            query.setFilterQueries(fq);
        }
        String responseFormat = params.get(WRITER_TYPE.getRequestParamName());
        query.set(WRITER_TYPE_PARAMETER, responseFormat);
        String start = params.get(START.getRequestParamName());
        query.setStart(Integer.valueOf(start));
        String rows = params.get(PAGE_SIZE.getRequestParamName());
        query.setRows(Integer.valueOf(rows));
        String sortStr = params.get(SORT.getRequestParamName());
        StringTokenizer st = new StringTokenizer(sortStr, " ");
        String sortField = st.nextToken();
        String sortDir = st.nextToken();
        query.setSort(sortField, sortDir.equals("asc") ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        String fieldsToReturn = params.get(FIELDS_TO_RETURN.getRequestParamName());
        fieldsToReturn = mergeField(sortField, fieldsToReturn);
        String fieldsToReturnArr[] = fieldsToReturn.split(" ");
        query.setFields(fieldsToReturnArr);

        log.debug("Sending query: q={} fq={} start={} rows={} sort={} fl={}", q, fq, start, rows, sortStr,
            fieldsToReturn);

        QueryResponse response = null;
        try
        {
            response = testServer.getServer().query(query);
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
     *                  org.outermedia.solrfusion.configuration.Configuration, org.outermedia.solrfusion.configuration.SearchServerConfig)}
     *                  )
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
        corePath = config.getUrl();
    }

}

