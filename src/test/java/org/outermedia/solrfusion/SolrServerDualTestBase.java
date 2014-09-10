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
