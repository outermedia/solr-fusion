package org.outermedia.solrfusion;

import org.apache.solr.client.solrj.SolrServer;
import org.junit.After;
import org.junit.Before;

/**
 * Created by stephan on 03.06.14.
 */
public class SolrServerDualTestBase {

    private SolrTestServer firstTestServer;
    protected SolrServer firstServer;

    private SolrTestServer secondTestServer;
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

//        // TODO: prepare index for tests
//        SolrInputDocument document = new SolrInputDocument();
//        document.addField("id", String.valueOf(1));
//        document.addField("title", String.valueOf("Troilus und Cressida"));
//        document.addField("author", String.valueOf("Shakespeare"));
//        firstServer.add(document);
//        firstTestServer.commitLastDocs();
//
//        SolrQuery query = new SolrQuery("*:*");
//        query.setRows(Integer.MAX_VALUE);
//        query.addField("title");
//        query.addField("author");
//        query.addField("id");
//        QueryResponse response = firstServer.query(query);
//        System.out.println(response.toString());
//
//        document = new SolrInputDocument();
//        document.addField("id", String.valueOf(1));
//        document.addField("title", String.valueOf("Titus Andronicus"));
//        document.addField("author", String.valueOf("Shakespeare"));
//        secondServer.add(document);
//        secondTestServer.commitLastDocs();
//
//        query = new SolrQuery("*:*");
//        query.setRows(Integer.MAX_VALUE);
//        query.addField("title");
//        query.addField("author");
//        query.addField("id");
//        response = secondServer.query(query);
//        System.out.println(response.toString());

    }

    @After
    public void shutdown()
    {
        firstServer.shutdown();
    }

}
