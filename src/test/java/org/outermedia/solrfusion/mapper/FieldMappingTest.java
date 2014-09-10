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
import org.outermedia.solrfusion.configuration.*;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 6/17/14.
 */
public class FieldMappingTest
{
    static class TestFieldMapping extends FieldMapping
    {
        @Override
        protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
        {
            super.afterUnmarshal(u, parent);
        }
    }

    protected TestFieldMapping createFieldMapping(String searchServersName, String fusionName,
            String searchServersNamePattern, String fusionNameReplacement, String searchServersNameReplacement,
            String fusionNamePattern)
    {
        TestFieldMapping fm = new TestFieldMapping();
        fm.setSearchServersName(searchServersName);
        fm.setFusionName(fusionName);
        fm.setSearchServersNamePattern(searchServersNamePattern);
        fm.setFusionNameReplacement(fusionNameReplacement);
        fm.setSearchServersNameReplacement(searchServersNameReplacement);
        fm.setFusionNamePattern(fusionNamePattern);
        return fm;
    }

    @Test
    public void testValidAttributeCombinations()
    {
        assertMappingType(createFieldMapping("a", null, null, null, null, null), MappingType.EXACT_NAME_ONLY);
        assertMappingType(createFieldMapping("a*", null, null, null, null, null), MappingType.WILDCARD_NAME_ONLY);
        assertMappingType(createFieldMapping(null, "a", null, null, null, null), MappingType.EXACT_FUSION_NAME_ONLY);
        assertMappingType(createFieldMapping(null, "a*", null, null, null, null), MappingType.WILDCARD_FUSION_NAME_ONLY);
        assertMappingType(createFieldMapping("a", "b", null, null, null, null), MappingType.EXACT_NAME_AND_FUSION_NAME);
        assertMappingType(createFieldMapping("a*", "b*", null, null, null, null), MappingType.WILDCARD_NAME_AND_FUSION_NAME);
        assertMappingType(createFieldMapping(null, null, "a", "b", "c", "d"), MappingType.REG_EXP_ALL);
        assertMappingType(createFieldMapping(null, null, "a", null, null, null), MappingType.REG_EXP_NAME_ONLY);
        assertMappingType(createFieldMapping(null, null, null, null, null, "d"), MappingType.REG_EXP_FUSION_NAME_ONLY);
    }

    @Test
    public void testInvalidAttributeCombinations()
    {
        assertException(createFieldMapping(null, null, null, null, null, null));

        assertException(createFieldMapping("a", null, "b", null, null, null));
        assertException(createFieldMapping("a*", null, "b", null, null, null));
        assertException(createFieldMapping(null, "a", "b", null, null, null));
        assertException(createFieldMapping(null, "a*", "b", null, null, null));
        assertException(createFieldMapping("a", null, null, "b", null, null));
        assertException(createFieldMapping("a*", null, null, "b", null, null));
        assertException(createFieldMapping(null, "a", null, "b", null, null));
        assertException(createFieldMapping(null, "a*", null, "b", null, null));
        assertException(createFieldMapping("a", null, null, null, "b", null));
        assertException(createFieldMapping("a*", null, null, null, "b", null));
        assertException(createFieldMapping(null, "a", null, null, "b", null));
        assertException(createFieldMapping(null, "a*", null, null, "b", null));
        assertException(createFieldMapping("a", null, null, null, null, "b"));
        assertException(createFieldMapping("a*", null, null, null, null, "b"));
        assertException(createFieldMapping(null, "a", null, null, null, "b"));
        assertException(createFieldMapping(null, "a*", null, null, null, "b"));

        assertException(createFieldMapping(null, null, null, "b", null, null));
        assertException(createFieldMapping(null, null, null, null, "b", null));

        assertException(createFieldMapping(null, null, "a", "b", null, null));
        assertException(createFieldMapping(null, null, "a", null, "b", null));
        assertException(createFieldMapping(null, null, "a", null, null, "b"));
        assertException(createFieldMapping(null, null, null, "a", "b", null));
        assertException(createFieldMapping(null, null, null, "a", null, "b"));
        assertException(createFieldMapping(null, null, null, null, "a", "b"));

        assertException(createFieldMapping(null, null, "a", "b", "c", null));
        assertException(createFieldMapping(null, null, "a", "b", null, "c"));
        assertException(createFieldMapping(null, null, "a", null, "b", "c"));
        assertException(createFieldMapping(null, null, null, "a", "b", "c"));

        assertException(createFieldMapping("A", "B", "a", "b", "c", "d"));
        assertException(createFieldMapping("A*", "B", "a", "b", "c", "d"));
        assertException(createFieldMapping("A", "B*", "a", "b", "c", "d"));
        assertException(createFieldMapping("A*", "B", "a", "b", "c", "d"));
    }

    private void assertException(TestFieldMapping fm)
    {
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.fail("Expected an exception");
        }
        catch (UnmarshalException e)
        {
            // OK
        }
    }

    protected void assertMappingType(TestFieldMapping fm, MappingType mappingType)
    {
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.assertEquals("Got wrong match type", mappingType, fm.getMappingType());
        }
        catch (UnmarshalException e)
        {
            Assert.fail("Expected no exception " + e);
        }
    }

    @Test
    public void testLiteralMapping() throws UnmarshalException
    {
        TestFieldMapping fm = createFieldMapping("a", "fb", null, null, null, null);
        fm.afterUnmarshal(null, null);

        ApplicableResult a = fm.applicableToSearchServerField("a");
        boolean ok = a != null;
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Search server field was mapped to different fusion field than expected", "fb",
                a.getDestinationFieldName());

        a = fm.applicableToFusionField("fb");
        ok = a != null;
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Fusion field was mapped to different search server field than expected", "a",
                a.getDestinationFieldName());

        a = fm.applicableToFusionField("fc");
        ok = a !=null;
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);

        a = fm.applicableToSearchServerField("b");
        ok = a != null;
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);
    }

    @Test
    public void testRegExpMapping() throws UnmarshalException
    {
        TestFieldMapping fm = createFieldMapping(null, null, "VAL([0-9]+)START", "from$1", "VAL$1START",
                "from([0-9]+)");
        fm.afterUnmarshal(null, null);

        ApplicableResult ar = fm.applicableToSearchServerField("VAL4START");
        Assert.assertTrue("RegExp failed, but should match", ar != null);
        Assert.assertEquals("Search server field was mapped to different fusion field than expected", "from4",
                ar.getDestinationFieldName());

        ar = fm.applicableToFusionField("from52");
        Assert.assertTrue("RegExp failed, but should match", ar != null);
        Assert.assertEquals("Fusion field was mapped to different search server field than expected", "VAL52START",
                ar.getDestinationFieldName());

        ar = fm.applicableToSearchServerField("VAL4END");
        Assert.assertFalse("RegExp match succeeded, but should fail", ar != null);

        ar = fm.applicableToFusionField("end52");
        Assert.assertFalse("RegExp match succeeded, but should fail", ar != null);
    }

    @Test
    public void testFusionDropResponse()
    {
        TestFieldMapping fm = createFieldMapping(null, "fn1", null, null, null, null);
        List<Operation> ops = new ArrayList<>();
        DropOperation o = new DropOperation();
        List<Target> ts = new ArrayList<>();
        ts.add(new Response());
        o.setTargets(ts);
        ops.add(o);
        fm.setOperations(ops);
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.fail("Expected exception for drop fusion name in response");
        }
        catch (Exception e)
        {
            // NOP
        }
    }

    @Test
    public void testNameDropQuery()
    {
        TestFieldMapping fm = createFieldMapping("n1", null, null, null, null, null);
        List<Operation> ops = new ArrayList<>();
        DropOperation o = new DropOperation();
        List<Target> ts = new ArrayList<>();
        ts.add(new Query());
        o.setTargets(ts);
        ops.add(o);
        fm.setOperations(ops);
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.fail("Expected exception for drop search server name in query");
        }
        catch (Exception e)
        {
            // NOP
        }
    }
}
