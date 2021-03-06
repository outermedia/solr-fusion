package org.outermedia.solrfusion.configuration;

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
