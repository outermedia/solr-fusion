package org.outermedia.solrfusion.mapper;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by ballmann on 7/10/14.
 */
public class TermTest
{
    @Test
    public void testCompare()
    {
        Term t0 = Term.newFusionTerm("f1");
        Term t1 = Term.newFusionTerm("f1", "a");
        Term t1a = Term.newFusionTerm("f1", "b");
        Term t2 = Term.newFusionTerm("f1", "a", "b");
        Term tNull = Term.newFusionTerm("f1");
        tNull.setFusionFieldValue(null);

        t0.compareFusionValue(null);

        tNull.compareFusionValue(null);
        tNull.compareFusionValue(t0);
        tNull.compareFusionValue(t1);
        tNull.compareFusionValue(t2);

        t0.compareFusionValue(t0);
        t0.compareFusionValue(t1);
        t0.compareFusionValue(t2);
        t0.compareFusionValue(tNull);

        t1.compareFusionValue(t0);
        Assert.assertEquals("Wrong comparison", 0, t1.compareFusionValue(t1));
        Assert.assertEquals("Wrong comparison", -1, t1.compareFusionValue(t1a));
        Assert.assertEquals("Wrong comparison", 1, t1a.compareFusionValue(t1));
        t1.compareFusionValue(t2);
        t1.compareFusionValue(tNull);

        t2.compareFusionValue(t0);
        t2.compareFusionValue(t1);
        t2.compareFusionValue(t2);
        Assert.assertEquals("Wrong comparison", -1, t2.compareFusionValue(t1a));
        Assert.assertEquals("Wrong comparison", 1, t1a.compareFusionValue(t2));
        t2.compareFusionValue(tNull);
    }

    @Test
    public void testFusionValueJoin()
    {
        Term t1 = Term.newFusionTerm("f1");
        String t1Joined = t1.mergeFusionValues();
        Assert.assertEquals("Merged values are different", "", t1Joined);

        t1 = Term.newFusionTerm("f1", "a");
        t1Joined = t1.mergeFusionValues();
        Assert.assertEquals("Merged values are different", "a", t1Joined);

        t1 = Term.newFusionTerm("f1", "a", "b");
        t1Joined = t1.mergeFusionValues();
        Assert.assertEquals("Merged values are different", "a,b", t1Joined);

        t1Joined = t1.mergeSearchServerValues();
        Assert.assertNull("Merged values are different", t1Joined);
    }

    @Test
    public void testSearchServerValueJoin()
    {
        Term t1 = Term.newSearchServerTerm("f1");
        String t1Joined = t1.mergeSearchServerValues();
        Assert.assertEquals("Merged values are different", "", t1Joined);

        t1 = Term.newSearchServerTerm("f1", "a");
        t1Joined = t1.mergeSearchServerValues();
        Assert.assertEquals("Merged values are different", "a", t1Joined);

        t1 = Term.newSearchServerTerm("f1", "a", "b");
        t1Joined = t1.mergeSearchServerValues();
        Assert.assertEquals("Merged values are different", "a,b", t1Joined);

        t1Joined = t1.mergeFusionValues();
        Assert.assertNull("Merged values are different", t1Joined);
    }
}
