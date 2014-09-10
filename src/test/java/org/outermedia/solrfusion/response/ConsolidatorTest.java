package org.outermedia.solrfusion.response;

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

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.adapter.SearchServerResponseException;
import org.outermedia.solrfusion.response.parser.XmlResponse;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 6/6/14.
 */
@SuppressWarnings("unchecked")
public class ConsolidatorTest
{

    @Test
    public void simpleMergeTest()
    {
        List<String> elements = new ArrayList(Arrays.asList("a", "b", "c"));
        TestClosableStringIterator t1 = new TestClosableStringIterator(elements);
        RoundRobinClosableIterator rr = new RoundRobinClosableIterator(new ArrayList(Arrays.asList(t1)), null);
        expectedList(elements, rr);
        Assert.assertTrue("close() not called for first iterator", t1.calledClose);
    }

    protected void expectedList(List<String> elements, RoundRobinClosableIterator rr)
    {
        // System.out.println("---");
        for (String s : elements)
        {
            // System.out.println("AT " + s);
            Assert.assertTrue("Expected to find element: " + s, rr.hasNext());
            Assert.assertEquals("Found different element than expected", s, rr.next());
        }
        Assert.assertFalse("Expected to find no further element", rr.hasNext());
    }

    @Test
    public void merge2IteratorsTest()
    {
        List<String> elements1 = new ArrayList(Arrays.asList("a", "b", "c"));
        TestClosableStringIterator t1 = new TestClosableStringIterator(elements1);

        List<String> elements2 = new ArrayList(Arrays.asList("A", "B"));
        TestClosableStringIterator t2 = new TestClosableStringIterator(elements2);

        RoundRobinClosableIterator rr = new RoundRobinClosableIterator(new ArrayList(Arrays.asList(t1, t2)), null);
        List<String> expected = Arrays.asList("a", "A", "b", "B", "c");
        expectedList(expected, rr);
        Assert.assertTrue("close() not called for first iterator", t1.calledClose);
        Assert.assertTrue("close() not called for second iterator", t2.calledClose);

        t1 = new TestClosableStringIterator(elements1);
        t2 = new TestClosableStringIterator(elements2);
        rr = new RoundRobinClosableIterator(new ArrayList(Arrays.asList(t2, t1)), null);
        expected = Arrays.asList("A", "a", "B", "b", "c");
        expectedList(expected, rr);
        Assert.assertTrue("close() not called for first iterator", t1.calledClose);
        Assert.assertTrue("close() not called for second iterator", t2.calledClose);
    }

    @Test
    public void closeTest()
    {
        List<String> elements = new ArrayList(Arrays.asList("a", "b", "c"));
        TestClosableStringIterator t1 = new TestClosableStringIterator(elements);
        RoundRobinClosableIterator rr = new RoundRobinClosableIterator(new ArrayList(Arrays.asList(t1)), null);
        rr.close();
        Assert.assertFalse("Expected no element after close()", rr.hasNext());
        Assert.assertTrue("close() not called for second iterator", t1.calledClose);
    }

    @Test
    public void testErrorHandling()
        throws SAXException, JAXBException, ParserConfigurationException, FileNotFoundException
    {
        ResponseConsolidatorIfc rc = PagingResponseConsolidator.Factory.getInstance();
        String case2Response = "<response>\n" +
            "    <lst name=\"responseHeader\">\n" +
            "        <int name=\"status\">500</int>\n" +
            "        <int name=\"QTime\">2</int>\n" +
            "        <lst name=\"params\">\n" +
            "            <str name=\"q\">flubb:*</str>\n" +
            "            <str name=\"fq\">(collection:GVK OR collection:DOAJ OR (collection_details:ZDB-1-PIO))</str>\n" +
            "        </lst>\n" +
            "    </lst>\n" +
            "    <lst name=\"error\">\n" +
            "        <str name=\"msg\">undefined field flubb</str>\n" +
            "        <int name=\"code\">500</int>\n" +
            "    </lst>\n" +
            "</response>";
        SearchServerResponseException case1 = new SearchServerResponseException(400, "Case1", null);
        rc.addErrorResponse(case1);

        InputStream httpContent = new StringBufferInputStream(case2Response);
        // 400 should be hidden by 500 contained in case2Response
        SearchServerResponseException case2 = new SearchServerResponseException(400, "Case2", httpContent);
        rc.addErrorResponse(case2);
        ResponseParserIfc responseParser = DefaultResponseParser.Factory.getInstance();
        XmlResponse responseError = responseParser.parse(httpContent);
        // System.out.println("RESPONSE "+responseError);
        case2.setResponseError(responseError.getResponseErrors());
        String errorMsg = rc.getErrorMsg();
        Assert.assertEquals("", "ERROR 400: Case1\nERROR 500: undefined field flubb", errorMsg.trim());
    }
}
