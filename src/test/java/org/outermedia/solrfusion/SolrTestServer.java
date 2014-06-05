package org.outermedia.solrfusion;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.ConfigSolr;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrResourceLoader;

import java.io.File;
import java.io.IOException;

public class SolrTestServer
{
//    private final static Logger logger = Logger.getLogger(SolrTestServer.class);

    private SolrServer server;
    private CoreContainer cc;

    private String solrHome;

    private String solrXmlFile;


    public SolrTestServer(String solrHome) throws Exception
    {
        this(solrHome, "solr.xml", "solr");
    }

    public SolrTestServer(String solrHome, String solrXmlFile) throws Exception
    {
        this(solrHome, solrXmlFile, "solr");
    }

    public SolrTestServer(String solrHome, String solrXmlFile, String coreName) throws Exception
    {
        this.solrHome = solrHome;
        this.solrXmlFile = solrXmlFile;
        SolrResourceLoader loader = new SolrResourceLoader(solrHome);
        ConfigSolr config = ConfigSolr.fromFile(loader, getSolrXmlFile());

        cc = new CoreContainer(loader, config);
        cc.load();
        server = new EmbeddedSolrServer(cc, coreName);
    }

    public SolrServer getServer()
    {
        return server;
    }

    public void finish() throws Exception
    {
        clean();
        if (cc != null)
            cc.shutdown();
    }

    public void clean() throws SolrServerException, IOException
    {
        if (server instanceof EmbeddedSolrServer)
        {
            removeAllSolrDocs();
        }
    }

    public void removeAllSolrDocs() throws SolrServerException, IOException
    {
        System.out.println("=================================== SOLRTESTHELPER REMOVE ALL DOCS");
        server.deleteByQuery("*:*"); // deletes everything!
        commitLastDocs();
    }

    public void commitLastDocs()
    {
        try
        {
            server.commit(true, true);
        } catch (Exception e)
        {
//            logger.error("Caught exception while committing", e);
        }
    }

    public SolrQuery createQuery(String query)
    {
        SolrQuery sq = new SolrQuery(query);
        sq.setFields("*");
        sq.setRows(Integer.MAX_VALUE);
        return sq;
    }

    public QueryResponse query(SolrQuery sq) throws SolrServerException
    {
        return server.query(sq, METHOD.POST);
    }

    public File getSolrXmlFile()
    {
        return new File(solrHome, solrXmlFile);
    }

    public long getDocumentCount() throws SolrServerException
    {
        SolrQuery query = createQuery("*:*");
        QueryResponse result = server.query(query);
        return result.getResults().getNumFound();
    }

    public SolrDocumentList getDocs() throws SolrServerException
    {
        SolrQuery query = createQuery("*:*");
        QueryResponse result = server.query(query);
        return result.getResults();
    }

}