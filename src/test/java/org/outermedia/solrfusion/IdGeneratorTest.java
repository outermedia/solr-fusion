package org.outermedia.solrfusion;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ballmann on 7/10/14.
 */
public class IdGeneratorTest
{
    @Test
    public void testExtraction()
    {
        IdGeneratorIfc idGen = DefaultIdGenerator.Factory.getInstance();
        // "#" is currently the separator!
        String serverName = "ab c";
        String searchServerDocId = "3" + DefaultIdGenerator.SEPARATOR + "2";
        String fid = idGen.computeId(serverName, searchServerDocId);
        Assert.assertEquals("Got wrong server name", serverName, idGen.getSearchServerIdFromFusionId(fid));
        Assert.assertEquals("Got wrong doc id", searchServerDocId, idGen.getSearchServerDocIdFromFusionId(fid));

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
}
