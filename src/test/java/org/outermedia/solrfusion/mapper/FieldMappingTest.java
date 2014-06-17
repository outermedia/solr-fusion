package org.outermedia.solrfusion.mapper;

import junit.framework.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.FieldMapping;
import org.outermedia.solrfusion.configuration.MatchType;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

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
        // case 1
        assertLiteral(createFieldMapping("a", null, null, null, null, null));
        // case 2
        assertLiteral(createFieldMapping(null, "a", null, null, null, null));
        // case 3
        assertLiteral(createFieldMapping("a", "b", null, null, null, null));
        // case 4
        assertRegExp(createFieldMapping(null, null, "a", "b", "c", "d"));
    }

    @Test
    public void testInvalidAttributeCombinations()
    {
        assertException(createFieldMapping(null, null, null, null, null, null));

        assertException(createFieldMapping("a", null, "b", null, null, null));
        assertException(createFieldMapping(null, "a", "b", null, null, null));
        assertException(createFieldMapping("a", null, null, "b", null, null));
        assertException(createFieldMapping(null, "a", null, "b", null, null));
        assertException(createFieldMapping("a", null, null, null, "b", null));
        assertException(createFieldMapping(null, "a", null, null, "b", null));
        assertException(createFieldMapping("a", null, null, null, null, "b"));
        assertException(createFieldMapping(null, "a", null, null, null, "b"));

        assertException(createFieldMapping(null, null, "b", null, null, null));
        assertException(createFieldMapping(null, null, null, "b", null, null));
        assertException(createFieldMapping(null, null, null, null, "b", null));
        assertException(createFieldMapping(null, null, null, null, null, "b"));

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

    protected void assertLiteral(TestFieldMapping fm)
    {
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.assertEquals("Got wrong match type", MatchType.LITERAL, fm.getMatchType());
        }
        catch (UnmarshalException e)
        {
            Assert.fail("Expected no exception " + e);
        }
    }

    protected void assertRegExp(TestFieldMapping fm)
    {
        try
        {
            fm.afterUnmarshal(null, null);
            Assert.assertEquals("Got wrong match type", MatchType.REG_EXP, fm.getMatchType());
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

        boolean ok = fm.applicableToSearchServerField("a");
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Search server field was mapped to different fusion field than expected", "fb",
                fm.getSpecificFusionName());

        ok = fm.applicableToFusionField("fb");
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Fusion field was mapped to different search server field than expected", "a",
                fm.getSpecificSearchServerName());

        ok = fm.applicableToFusionField("fc");
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);

        ok = fm.applicableToSearchServerField("b");
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);
    }

    @Test
    public void testRegExpMapping() throws UnmarshalException
    {
        TestFieldMapping fm = createFieldMapping(null, null, "VAL([0-9]+)START", "from$1", "VAL$1START",
                "from([0-9]+)");
        fm.afterUnmarshal(null, null);

        boolean ok = fm.applicableToSearchServerField("VAL4START");
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Search server field was mapped to different fusion field than expected", "from4",
                fm.getSpecificFusionName());

        ok = fm.applicableToFusionField("from52");
        Assert.assertTrue("RegExp failed, but should match", ok);
        Assert.assertEquals("Fusion field was mapped to different search server field than expected", "VAL52START",
                fm.getSpecificSearchServerName());

        ok = fm.applicableToSearchServerField("VAL4END");
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);

        ok = fm.applicableToFusionField("end52");
        Assert.assertFalse("RegExp match succeeded, but should fail", ok);
    }
}
