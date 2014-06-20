package org.outermedia.solrfusion.configuration;

import java.util.regex.Matcher;

/**
 * Created by ballmann on 6/17/14.
 */
public enum MatchType
{
    LITERAL, REG_EXP, WILDCARD;

    protected ApplicableResult applicableToFusionField(String fusionFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        String destinationField = null;
        switch (this)
        {
            case LITERAL:
                if (fusionFieldName.equals(fieldMapping.getFusionName()))
                {
                    destinationField = fieldMapping.getSearchServersName();
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField);
                }
                break;
            case REG_EXP:
                Matcher matcher = fieldMapping.getFusionNameRegExp().matcher(fusionFieldName);
                if (matcher.find())
                {
                    destinationField = matcher.replaceAll(fieldMapping.getSearchServersNameReplacement());
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField);
                }
                break;
            case WILDCARD:
                throw new RuntimeException("WILDCARD not supported");
        }
        return result;
    }

    protected ApplicableResult applicableToSearchServerField(String searchServerFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        String destinationField = null;
        switch (this)
        {
            case LITERAL:
                if (searchServerFieldName.equals(fieldMapping.getSearchServersName()))
                {
                    destinationField = fieldMapping.getFusionName();
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField);
                }
                break;
            case REG_EXP:
                Matcher matcher = fieldMapping.getSearchServersNameRegExp().matcher(searchServerFieldName);
                if (matcher.find())
                {
                    destinationField = matcher.replaceAll(fieldMapping.getFusionNameReplacement());
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField);
                }
                break;
            case WILDCARD:
                throw new RuntimeException("WILDCARD not supported");
        }
        return result;
    }

}
