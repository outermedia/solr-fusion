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

import org.junit.Before;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;

/**
 * Created by ballmann on 6/19/14.
 */
public class AbstractTypeTest
{
    protected TestHelper helper;
    String docOpen = "<om:core xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
            "         xmlns:om=\"http://solrfusion.outermedia.org/configuration/\"" +
            "         xmlns=\"http://solrfusion.outermedia.org/configuration/type/\">";
    String docClose = "</om:core>";

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    protected Document buildResponseDocument() {
        Document doc = new Document();
        return doc;
    }

    protected Term buildResponseField(Document doc, String name, String... value)
    {
        return doc.addField(name,value).getTerm();
    }
}
