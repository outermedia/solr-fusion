package org.outermedia.solrfusion.adapter.solr;

import junit.framework.Assert;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.outermedia.solrfusion.SolrServerDualTestBase;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.configuration.Configuration;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Created by stephan on 11.06.14.
 */
public class DefaultSolrAdapterTest extends SolrServerDualTestBase {

    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void testHttpClientGet() throws IOException, URISyntaxException {
        HttpClient client = HttpClientBuilder.create().build();
        URI uri = new URI("http", null, "www.outermedia.de", 80, "/", "q=bla", null);
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);

        Header[] header = response.getHeaders("Server");
        Assert.assertTrue("Server is Ubuntu", header[0].getValue().indexOf("Ubuntu") != -1);

        // Get the response
        HttpEntity entity = response.getEntity();
        Assert.assertEquals("Content-type is utf8 hmtl", "text/html; charset=utf-8", entity.getContentType().getValue());

        String content = new Scanner(response.getEntity().getContent(), "UTF-8").useDelimiter("\\A").next();
        System.out.println(content);

//        BufferedReader rd = new BufferedReader
//                (new InputStreamReader(response.getEntity().getContent()));
//        String line = "";
//        while ((line = rd.readLine()) != null) {
//            System.out.println(line);
//        }
    }

    @Test
    @Ignore
    public void testDefaultSolrAdapter() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException {

        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-solr-adapter-fusion-schema.xml");



    }
}
