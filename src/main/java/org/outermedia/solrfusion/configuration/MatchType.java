package org.outermedia.solrfusion.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the different types of field name matching which is used in mapping rules.
 *
 * Created by ballmann on 6/17/14.
 */
public enum MatchType
{
    LITERAL, REG_EXP, WILDCARD;

    /**
     * Check whether a mapping is applicable to a fusion field.
     *
     * @param fusionFieldName
     * @param fieldMapping
     * @return null if not applicable else an instance of ApplicableResult
     */
    public ApplicableResult applicableToFusionField(String fusionFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        String destinationField = null;
        switch (this)
        {
            case LITERAL:
                if (fusionFieldName.equalsIgnoreCase(fieldMapping.getFusionName()))
                {
                    destinationField = fieldMapping.getSearchServersName();
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField, (FieldMapping) null);
                }
                break;
            case REG_EXP:
                if (fusionFieldName != null)
                {
                    String searchServersNameReplacement = fieldMapping.getSearchServersNameReplacement();
                    Matcher matcher = fieldMapping.getFusionNameRegExp().matcher(fusionFieldName);
                    result = getApplicableResult(searchServersNameReplacement, matcher);
                }
                break;
            case WILDCARD:
                String mappingFusionFieldNamePattern = buildWildcardPattern(fieldMapping.getFusionName());
                String mappingSearchServerNameReplacement = buildWildcardReplacement(
                    fieldMapping.getSearchServersName());
                result = getApplicableResult(mappingSearchServerNameReplacement,
                    Pattern.compile(mappingFusionFieldNamePattern, Pattern.CASE_INSENSITIVE).matcher(fusionFieldName));
                break;
        }
        return result;
    }

    protected String buildWildcardPattern(String fieldPattern)
    {
        return "^" + fieldPattern.replace("*", "(.*)") + "$";
    }

    protected String buildWildcardReplacement(String replacement)
    {
        if (replacement != null)
        {
            int groupRef = 1;
            int pos = -1;
            while ((pos = replacement.indexOf('*')) >= 0)
            {
                replacement = replacement.substring(0, pos) + "$" + groupRef +
                    replacement.substring(pos + 1);
                groupRef++;
            }
        }
        return replacement;
    }

    protected ApplicableResult getApplicableResult(String replacement, Matcher matcher)
    {
        ApplicableResult result = null;
        String destinationField = null;
        if (matcher.find())
        {
            // no replacement for <drop>
            if (replacement != null)
            {
                destinationField = matcher.replaceAll(replacement);
            }
            // even is destination field is empty e.g. for a <drop>, but the field was mapped
            result = new ApplicableResult(destinationField, (FieldMapping) null);
        }
        return result;
    }

    /**
     * Check whether a mapping is applicable to a search server field.
     *
     * @param searchServerFieldName
     * @param fieldMapping
     * @return null if not applicable else an instance of ApplicableResult
     */
    public ApplicableResult applicableToSearchServerField(String searchServerFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        String destinationField = null;
        switch (this)
        {
            case LITERAL:
                if (fieldMapping.getSearchServersName().equalsIgnoreCase(searchServerFieldName))
                {
                    destinationField = fieldMapping.getFusionName();
                    // even is destination field is empty e.g. for a <drop>, but the field was mapped
                    result = new ApplicableResult(destinationField, (FieldMapping) null);
                }
                break;
            case REG_EXP:
                if (searchServerFieldName != null)
                {
                    String fusionNameReplacement = fieldMapping.getFusionNameReplacement();
                    Matcher matcher = fieldMapping.getSearchServersNameRegExp().matcher(searchServerFieldName);
                    result = getApplicableResult(fusionNameReplacement, matcher);
                }
                break;
            case WILDCARD:
                String mappingSearchServerNamePattern = buildWildcardPattern(fieldMapping.getSearchServersName());
                String mappingFusionNameReplacement = buildWildcardReplacement(fieldMapping.getFusionName());
                result = getApplicableResult(mappingFusionNameReplacement,
                    Pattern.compile(mappingSearchServerNamePattern, Pattern.CASE_INSENSITIVE).matcher(
                        searchServerFieldName));
                break;
        }
        return result;
    }

}
