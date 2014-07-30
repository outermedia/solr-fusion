package org.outermedia.solrfusion;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by ballmann on 7/30/14.
 */
public class MultiKeyAndValueMapTest
{
    @Test
    public void testConnect()
    {
        MultiKeyAndValueMap<String, Integer> map = new MultiKeyAndValueMap();
        map.resetIdCounter();

        map.put(Arrays.asList("1", "2", "3"), 41);
        System.out.println("1: " + map);
        map.put(Arrays.asList("4", "5", "6"), 42);
        System.out.println("2: " + map);

        // "3" and "4" connect the first two entries
        map.put(Arrays.asList("3", "4"), 43);
        System.out.println("3: " + map);

        String expected = "{\n" +
            "\t3=2@[42, 43, 41]\n" +
            "\t2=2@[42, 43, 41]\n" +
            "\t1=2@[42, 43, 41]\n" +
            "\t6=2@[42, 43, 41]\n" +
            "\t5=2@[42, 43, 41]\n" +
            "\t4=2@[42, 43, 41]\n" +
            "}";

        Assert.assertEquals("Got different map than expected", expected, map.toString());
    }

    @Test
    public void testMerge()
    {
        MultiKeyAndValueMap<String, Integer> map = new MultiKeyAndValueMap();
        map.resetIdCounter();

        map.put(Arrays.asList("1", "2", "3"), 41);
        System.out.println("1: " + map);
        map.put(Arrays.asList("2", "3", "4"), 42);
        System.out.println("2: " + map);

        String expected = "{\n" +
            "\t3=1@[42, 41]\n" +
            "\t2=1@[42, 41]\n" +
            "\t1=1@[42, 41]\n" +
            "\t4=1@[42, 41]\n" +
            "}";

        Assert.assertEquals("Got different map than expected", expected, map.toString());
    }
}
