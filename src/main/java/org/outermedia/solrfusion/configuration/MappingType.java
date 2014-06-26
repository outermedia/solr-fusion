package org.outermedia.solrfusion.configuration;

/**
 * Created by ballmann on 6/26/14.
 */
public enum MappingType
{
    UNKNOWN(MatchType.LITERAL), EXACT_NAME_ONLY(MatchType.LITERAL), EXACT_FUSION_NAME_ONLY(MatchType.LITERAL),
    EXACT_NAME_AND_FUSION_NAME(MatchType.LITERAL), REG_EXP_ALL(MatchType.REG_EXP),
    REG_EXP_NAME_ONLY(MatchType.REG_EXP), REG_EXP_FUSION_NAME_ONLY(MatchType.REG_EXP);

    protected MatchType matchType;

    MappingType(MatchType matchType)
    {
        this.matchType = matchType;
    }

    protected static MappingType getMappingType(boolean nameSet, boolean fusionSet, boolean namePatSet,
            boolean fusionReplSet, boolean nameReplSet, boolean fusionPatSet)
    {
        MappingType result = UNKNOWN;
        if (nameSet && !fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = EXACT_NAME_ONLY;
        }
        else if (!nameSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = EXACT_FUSION_NAME_ONLY;
        }
        else if (nameSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = EXACT_NAME_AND_FUSION_NAME;
        }
        else if (!nameSet && !fusionSet && namePatSet && fusionReplSet && nameReplSet && fusionPatSet)
        {
            result = REG_EXP_ALL;
        }
        else if (!nameSet && !fusionSet && namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = REG_EXP_NAME_ONLY;
        }
        else if (!nameSet && !fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && fusionPatSet)
        {
            result = REG_EXP_FUSION_NAME_ONLY;
        }
        return result;
    }

    protected ApplicableResult applicableToFusionField(String fusionFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        if (this == EXACT_FUSION_NAME_ONLY || this == EXACT_NAME_AND_FUSION_NAME ||
                this == REG_EXP_FUSION_NAME_ONLY || this == REG_EXP_ALL)
        {
            result = matchType.applicableToFusionField(fusionFieldName, fieldMapping);
        }
        return result;
    }

    protected ApplicableResult applicableToSearchServerField(String searchServerFieldName,
            FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        if (this == EXACT_NAME_ONLY || this == EXACT_NAME_AND_FUSION_NAME ||
                this == REG_EXP_NAME_ONLY || this == REG_EXP_ALL)
        {
            result = matchType.applicableToSearchServerField(searchServerFieldName, fieldMapping);
        }
        return result;
    }

    protected boolean isFusionFieldOnly()
    {
        return equals(EXACT_FUSION_NAME_ONLY) || equals(REG_EXP_FUSION_NAME_ONLY);
    }

    protected boolean isSearchServerFieldOnly()
    {
        return equals(EXACT_NAME_ONLY) || equals(REG_EXP_NAME_ONLY);
    }
}
