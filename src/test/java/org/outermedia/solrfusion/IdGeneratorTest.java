package org.outermedia.solrfusion;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ballmann on 7/10/14.
 */
public class IdGeneratorTest
{
    @Test
    public void testExtraction()
    {
        IdGeneratorIfc idGen = DefaultIdGenerator.Factory.getInstance();
        String serverName = "abc";
        String searchServerDocId = "3" + DefaultIdGenerator.SEPARATOR + "2";
        String fid = idGen.computeId(serverName, searchServerDocId);
        System.out.println("CID " + fid);
        Assert.assertEquals("Got wrong server name", serverName, idGen.getSearchServerIdFromFusionId(fid));
        Assert.assertEquals("Got wrong doc id", searchServerDocId, idGen.getSearchServerDocIdFromFusionId(fid));

        // for merged ids the first entry should be used
        String fid1 = idGen.computeId("Server2", "4");
        String mergedId = idGen.mergeIds(fid, fid1);
        Assert.assertEquals("Got wrong server name", serverName, idGen.getSearchServerIdFromFusionId(mergedId));
        Assert.assertEquals("Got wrong doc id", searchServerDocId, idGen.getSearchServerDocIdFromFusionId(mergedId));

        try
        {
            idGen.computeId(DefaultIdGenerator.SEPARATOR, "");
            Assert.fail("Expected exception, because server name contains '#'");
        }
        catch (RuntimeException e)
        {
            // NOP
        }
    }

    @Test
    public void testMergeSplitIds()
    {
        IdGeneratorIfc idGen = DefaultIdGenerator.Factory.getInstance();
        String thisId = idGen.computeId("ServerA", "1");
        String otherId1 = idGen.computeId("ServerB", "2");
        String otherId2 = idGen.computeId("ServerC", "3");
        String mergedId = idGen.mergeIds(idGen.mergeIds(thisId, otherId1), otherId2);
        String sep = DefaultIdGenerator.SEPARATOR;
        String isep = DefaultIdGenerator.ID_SEPARATOR;

        Assert.assertEquals("Got different merged doc id",
            "ServerA" + sep + "1" + isep + "ServerB" + sep + "2" + isep + "ServerC" + sep + "3", mergedId);

        List<String> splitIds = idGen.splitMergedId(mergedId);
        List<String> expectedList = Arrays.asList("ServerA" + sep + "1", "ServerB" + sep + "2",
            "ServerC" + sep + "3");
        Assert.assertEquals("Got different split doc ids", expectedList, splitIds);
    }
}
