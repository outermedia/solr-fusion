package org.outermedia.solrfusion.adapter.solr;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.outermedia.solrfusion.SolrTestServer;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.adapter.SearchServerAdapterIfc;
import org.outermedia.solrfusion.configuration.SearchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * This class is able to send requests to a solr server and to receive answers.
 *
 * @author stephan
 *
 */
@ToString
public class EmbeddedSolrAdapter implements SearchServerAdapterIfc {

    @Setter @Getter private SolrTestServer testServer;

    String corePath;

    /**
     * Factory creates instances only.
     */
    private EmbeddedSolrAdapter ()
    {}

    @Override
    public InputStream sendQuery(String searchServerQueryStr) throws URISyntaxException, IOException  {
        return sendQuery(searchServerQueryStr, 0);
    }

    @Override
    public InputStream sendQuery(String searchServerQueryStr, int timeout) throws URISyntaxException, IOException  {
        SolrQuery query = new SolrQuery(searchServerQueryStr);
//        query.setRows(10);
//        query.addField("id");
//        query.addField("author");
//        query.addField("score");
        QueryResponse response = null;
        try {
            response = testServer.getServer().query(query);
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        InputStream inputStream = TestHelper.embeddedQueryToXmlInputStream(query, response);

        return inputStream;
    }

    public static class Factory
    {
        public static EmbeddedSolrAdapter getInstance()
        {
            return new EmbeddedSolrAdapter();
        }
    }

    @Override
    public void init(SearchServerConfig config) {
        corePath = config.getUrl();
    }

}

