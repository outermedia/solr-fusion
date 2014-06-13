package org.outermedia.solrfusion;

import org.apache.solr.client.solrj.SolrServer;
import org.junit.After;
import org.junit.Before;

/**
 * Created by stephan on 03.06.14.
 */
public class SolrServerDualTestBase {

    protected SolrTestServer firstTestServer;
    protected SolrServer firstServer;

    protected SolrTestServer secondTestServer;
    protected SolrServer secondServer;

    @Before
    public void setupSolr() throws Exception
    {
        System.setProperty("om.solr.lib", "");
        System.setProperty("om.solr.validation.lib", "");
        System.setProperty("om.solr.lib", "lib");

        firstTestServer = new SolrTestServer("src/test/resources/solr/solr-home-1");
        firstServer = firstTestServer.getServer();

        secondTestServer = new SolrTestServer("src/test/resources/solr/solr-home-2");
        secondServer = secondTestServer.getServer();
    }

    @After
    public void shutdown() throws Exception {
        firstTestServer.finish();
        secondTestServer.finish();
    }

}
