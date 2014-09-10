package org.outermedia.solrfusion;

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
