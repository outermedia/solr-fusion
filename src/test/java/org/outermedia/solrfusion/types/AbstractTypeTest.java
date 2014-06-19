package org.outermedia.solrfusion.types;

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
