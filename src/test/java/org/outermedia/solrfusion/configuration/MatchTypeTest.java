package org.outermedia.solrfusion.configuration;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by ballmann on 8/21/14.
 */
public class MatchTypeTest
{
    @Test
    public void wildcardTest()
    {
        FieldMapping fm = new FieldMapping();
        fm.setFusionName("*title*");
        fm.setSearchServersName("*Titel*");

        // fusion field to search server field
        ApplicableResult res = MatchType.WILDCARD.applicableToFusionField("old_title_auth", fm);
        String searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "old_Titel_auth", searchServerField);

        // not matching fusion field
        res = MatchType.WILDCARD.applicableToFusionField("author", fm);
        Assert.assertNull("Expected no match", res);

        // search server field to fusion field
        res = MatchType.WILDCARD.applicableToSearchServerField("old_Titel_auth", fm);
        String fusionField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other fusion field", "old_title_auth", fusionField);

        // not matching search server field
        res = MatchType.WILDCARD.applicableToSearchServerField("author", fm);
        Assert.assertNull("Expected no match", res);

        // should match fully
        fm = new FieldMapping();
        fm.setFusionName("title*V");
        fm.setSearchServersName("Titel*X");
        res = MatchType.WILDCARD.applicableToFusionField("title5Vabc", fm);
        Assert.assertNull("Should not match", res);

        // empty replacement
        fm = new FieldMapping();
        fm.setFusionName("title*");
        fm.setSearchServersName("Titel*");
        res = MatchType.WILDCARD.applicableToFusionField("title", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "Titel", searchServerField);

        // match whole rest
        res = MatchType.WILDCARD.applicableToFusionField("title_auth", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "Titel_auth", searchServerField);

        // match all
        fm = new FieldMapping();
        fm.setFusionName("*");
        fm.setSearchServersName("*");
        res = MatchType.WILDCARD.applicableToFusionField("title", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "title", searchServerField);
    }

    @Test
    public void wildcardCaseInsensitiveTest()
    {
        FieldMapping fm = new FieldMapping();
        fm.setFusionName("*title*");
        fm.setSearchServersName("*Titel*");

        // fusion field to search server field
        ApplicableResult res = MatchType.WILDCARD.applicableToFusionField("old_TITLE_auth", fm);
        String searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "old_Titel_auth", searchServerField);

        // not matching fusion field
        res = MatchType.WILDCARD.applicableToFusionField("author", fm);
        Assert.assertNull("Expected no match", res);

        // search server field to fusion field
        res = MatchType.WILDCARD.applicableToSearchServerField("old_TITEL_auth", fm);
        String fusionField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other fusion field", "old_title_auth", fusionField);

        // not matching search server field
        res = MatchType.WILDCARD.applicableToSearchServerField("author", fm);
        Assert.assertNull("Expected no match", res);

        // should match fully
        fm = new FieldMapping();
        fm.setFusionName("title*V");
        fm.setSearchServersName("Titel*X");
        res = MatchType.WILDCARD.applicableToFusionField("TITLE5Vabc", fm);
        Assert.assertNull("Should not match", res);

        // empty replacement
        fm = new FieldMapping();
        fm.setFusionName("title*");
        fm.setSearchServersName("Titel*");
        res = MatchType.WILDCARD.applicableToFusionField("TITLE", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "Titel", searchServerField);

        // match whole rest
        res = MatchType.WILDCARD.applicableToFusionField("TITLE_auth", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "Titel_auth", searchServerField);

        // match all
        fm = new FieldMapping();
        fm.setFusionName("*");
        fm.setSearchServersName("*");
        res = MatchType.WILDCARD.applicableToFusionField("title", fm);
        searchServerField = res.getDestinationFieldName();
        Assert.assertEquals("Expected other search field", "title", searchServerField);
    }

    @Test
    public void testLiteralMatch()
    {
        FieldMapping fm = new FieldMapping();
        fm.setFusionName("title");
        fm.setSearchServersName("Titel");

        ApplicableResult res = MatchType.LITERAL.applicableToFusionField("TITLE", fm);
        Assert.assertEquals("Expected other search field", "Titel", res.getDestinationFieldName());

        res = MatchType.LITERAL.applicableToSearchServerField("TITEL", fm);
        Assert.assertEquals("Expected other fusion field", "title", res.getDestinationFieldName());
    }
}
