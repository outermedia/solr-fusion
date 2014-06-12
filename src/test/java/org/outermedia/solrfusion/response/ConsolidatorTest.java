package org.outermedia.solrfusion.response;

import org.junit.Assert;
import org.junit.Test;

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
}
