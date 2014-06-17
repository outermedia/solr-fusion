package org.outermedia.solrfusion.configuration;

import java.util.regex.Matcher;

/**
 * Created by ballmann on 6/17/14.
 */
public enum MatchType
{
    LITERAL, REG_EXP, WILDCARD;

    protected String applicableToFusionField(String fusionFieldName, FieldMapping fieldMapping)
    {
        String result = null;
        switch (this)
        {
            case LITERAL:
                if (fusionFieldName.equals(fieldMapping.getFusionName()))
                {
                    result = fieldMapping.getSearchServersName();
                }
                break;
            case REG_EXP:
                Matcher matcher = fieldMapping.getFusionNameRegExp().matcher(fusionFieldName);
                if (matcher.find())
                {
                    result = matcher.replaceAll(fieldMapping.getSearchServersNameReplacement());
                }
                break;
            case WILDCARD:
                throw new RuntimeException("WILDCARD not supported");
        }
        return result;
    }

    protected String applicableToSearchServerField(String searchServerFieldName, FieldMapping fieldMapping)
    {
        String result = null;
        switch (this)
        {
            case LITERAL:
                if (searchServerFieldName.equals(fieldMapping.getSearchServersName()))
                {
                    result = fieldMapping.getFusionName();
                }
                break;
            case REG_EXP:
                Matcher matcher = fieldMapping.getSearchServersNameRegExp().matcher(searchServerFieldName);
                if (matcher.find())
                {
                    result = matcher.replaceAll(fieldMapping.getFusionNameReplacement());
                }
                break;
            case WILDCARD:
                throw new RuntimeException("WILDCARD not supported");
        }
        return result;
    }

}
