package org.outermedia.solrfusion.mapper;

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
