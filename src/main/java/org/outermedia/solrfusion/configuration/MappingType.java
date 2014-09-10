package org.outermedia.solrfusion.configuration;

/**
 * This class enumerates all valid combinations of field names used in a mapping.
 *
 * Created by ballmann on 6/26/14.
 */
public enum MappingType
{
    UNKNOWN(MatchType.LITERAL), EXACT_NAME_ONLY(MatchType.LITERAL), EXACT_FUSION_NAME_ONLY(MatchType.LITERAL),
    EXACT_NAME_AND_FUSION_NAME(MatchType.LITERAL), REG_EXP_ALL(MatchType.REG_EXP),
    REG_EXP_NAME_ONLY(MatchType.REG_EXP), REG_EXP_FUSION_NAME_ONLY(MatchType.REG_EXP), WILDCARD_NAME_ONLY(
    MatchType.WILDCARD),
    WILDCARD_FUSION_NAME_ONLY(MatchType.WILDCARD), WILDCARD_NAME_AND_FUSION_NAME(MatchType.WILDCARD);

    protected MatchType matchType;

    MappingType(MatchType matchType)
    {
        this.matchType = matchType;
    }

    /**
     * Find out the matching mode of a mapping.
     * <p/>
     * nameSet excludes nameWildCardSet and vice versa. fusionSet excludes fusionWildCarSet and vice versa.
     *
     * @param nameSet
     * @param fusionSet
     * @param namePatSet
     * @param fusionReplSet
     * @param nameReplSet
     * @param fusionPatSet
     * @param nameWildCardSet
     * @param fusionWildCarSet
     * @return
     */
    protected static MappingType getMappingType(boolean nameSet, boolean fusionSet, boolean namePatSet,
        boolean fusionReplSet, boolean nameReplSet, boolean fusionPatSet, boolean nameWildCardSet,
        boolean fusionWildCarSet)
    {
        MappingType result = UNKNOWN;
        if (nameSet && !fusionSet && !fusionWildCarSet && !namePatSet && !fusionReplSet && !nameReplSet &&
            !fusionPatSet)
        {
            result = EXACT_NAME_ONLY;
        }
        else if (!nameSet && !nameWildCardSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet &&
            !fusionPatSet)
        {
            result = EXACT_FUSION_NAME_ONLY;
        }
        else if (nameSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = EXACT_NAME_AND_FUSION_NAME;
        }
        else if (!nameSet && !fusionSet && !nameWildCardSet && !fusionWildCarSet && namePatSet && fusionReplSet &&
            nameReplSet && fusionPatSet)
        {
            result = REG_EXP_ALL;
        }
        else if (!nameSet && !fusionSet && !nameWildCardSet && !fusionWildCarSet && namePatSet && !fusionReplSet &&
            !nameReplSet && !fusionPatSet)
        {
            result = REG_EXP_NAME_ONLY;
        }
        else if (!nameSet && !fusionSet && !nameWildCardSet && !fusionWildCarSet && !namePatSet && !fusionReplSet &&
            !nameReplSet && fusionPatSet)
        {
            result = REG_EXP_FUSION_NAME_ONLY;
        }
        else if (nameWildCardSet && !fusionSet && !fusionWildCarSet && !namePatSet && !fusionReplSet && !nameReplSet &&
            !fusionPatSet)
        {
            result = WILDCARD_NAME_ONLY;
        }
        else if (!nameWildCardSet && !nameSet && fusionWildCarSet && !namePatSet && !fusionReplSet && !nameReplSet &&
            !fusionPatSet)
        {
            result = WILDCARD_FUSION_NAME_ONLY;
        }
        else if (nameWildCardSet && fusionWildCarSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet)
        {
            result = WILDCARD_NAME_AND_FUSION_NAME;
        }
        return result;
    }

    protected ApplicableResult applicableToFusionField(String fusionFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        if (this == EXACT_FUSION_NAME_ONLY || this == EXACT_NAME_AND_FUSION_NAME ||
            this == REG_EXP_FUSION_NAME_ONLY || this == REG_EXP_ALL || this == WILDCARD_FUSION_NAME_ONLY ||
            this == WILDCARD_NAME_AND_FUSION_NAME)
        {
            result = matchType.applicableToFusionField(fusionFieldName, fieldMapping);
        }
        return result;
    }

    protected ApplicableResult applicableToSearchServerField(String searchServerFieldName, FieldMapping fieldMapping)
    {
        ApplicableResult result = null;
        if (this == EXACT_NAME_ONLY || this == EXACT_NAME_AND_FUSION_NAME ||
            this == REG_EXP_NAME_ONLY || this == REG_EXP_ALL || this == WILDCARD_NAME_ONLY ||
            this == WILDCARD_NAME_AND_FUSION_NAME)
        {
            result = matchType.applicableToSearchServerField(searchServerFieldName, fieldMapping);
        }
        return result;
    }

    protected boolean isFusionFieldOnly()
    {
        return equals(EXACT_FUSION_NAME_ONLY) || equals(REG_EXP_FUSION_NAME_ONLY) || equals(WILDCARD_FUSION_NAME_ONLY);
    }

    protected boolean isSearchServerFieldOnly()
    {
        return equals(EXACT_NAME_ONLY) || equals(REG_EXP_NAME_ONLY) || equals(WILDCARD_NAME_ONLY);
    }
}
