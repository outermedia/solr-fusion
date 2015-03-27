package org.outermedia.solrfusion.adapter.solr;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ballmann on 3/27/15.
 */
public class VersionTest
{

    @Test
    public void testEqual()
    {
        Assert.assertFalse("'lessThan' should return false for equal versions",
            new Version("4.2.1").lessThan(new Version("4.2.1")));
    }

    @Test
    public void testSameSizeLessThan()
    {
        Assert.assertTrue("'lessThan' should return true for lower versions (same size)",
            new Version("4.2.1").lessThan(new Version("4.2.2")));
        Assert.assertFalse("'lessThan' should return false for higer versions (same size)",
            new Version("4.2.2").lessThan(new Version("4.2.1")));
    }

    @Test
    public void testShorterSizeLessThan()
    {
        Assert.assertTrue("'lessThan' should return true for lower versions (shorter size)",
            new Version("4.2").lessThan(new Version("4.2.1")));
        Assert.assertFalse("'lessThan' should return false for higher versions (shorter size)",
            new Version("4.2.2").lessThan(new Version("4.2.1")));
    }

    @Test
    public void testLongerSizeLessThan()
    {
        Assert.assertFalse("'lessThan' should return false for lower versions (longer size)",
            new Version("4.2.1").lessThan(new Version("4.2")));
        Assert.assertTrue("'lessThan' should return true for lower versions (longer size)",
            new Version("4.1.9").lessThan(new Version("4.2")));
    }
}
