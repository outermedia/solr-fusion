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


import com.sun.xml.bind.annotation.XmlLocation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.mapper.UndeclaredFusionField;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.xml.sax.Locator;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Data holder class to store one field mapping configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldMapping", namespace = "http://solrfusion.outermedia.org/configuration/",
    propOrder = {"operations"})
@Getter
@Setter
@ToString(exclude = {"searchServersNameRegExp", "fusionNameRegExp", "locator"})
@Slf4j
public class FieldMapping
{
    @XmlAttribute(name = "name", required = false)
    private String searchServersName;

    @XmlAttribute(name = "fusion-name", required = false)
    private String fusionName;

    @XmlAttribute(name = "name-pattern", required = false)
    private String searchServersNamePattern;

    @XmlAttribute(name = "fusion-name-replacement", required = false)
    private String fusionNameReplacement;

    @XmlAttribute(name = "name-replacement", required = false)
    private String searchServersNameReplacement;

    @XmlAttribute(name = "fusion-name-pattern", required = false)
    private String fusionNamePattern;

    @XmlTransient
    private Pattern searchServersNameRegExp;

    @XmlTransient
    private Pattern fusionNameRegExp;

    @XmlTransient
    private MappingType mappingType;

    @XmlTransient @XmlLocation
    private Locator locator;


    @XmlElements(value = {@XmlElement(name = "add", type = AddOperation.class,
        namespace = "http://solrfusion.outermedia.org/configuration/"), @XmlElement(name = "drop",
        type = DropOperation.class,
        namespace = "http://solrfusion.outermedia.org/configuration/"), @XmlElement(name = "change",
        type = ChangeOperation.class,
        namespace = "http://solrfusion.outermedia.org/configuration/")})
    private List<Operation> operations;

    /**
     * Is 'this' mapping applicable to the specified fusion field? If true, then the field {@link
     * org.outermedia.solrfusion.configuration.ApplicableResult#destinationFieldName} is set to store the corresponding
     * search server field name which is maybe constructed by a regular expression or wildcard.
     *
     * @param fusionFieldName
     * @return true if applicable else false
     */
    public ApplicableResult applicableToFusionField(String fusionFieldName)
    {
        ApplicableResult result = mappingType.applicableToFusionField(fusionFieldName, this);
        if (result != null)
        {
            result.setMapping(this);
        }
        return result;
    }

    /**
     * This method applies 'this' mapping to a given query term.
     *  @param term
     * @param env
     * @param target
     */
    public void applyQueryOperations(Term term, ScriptEnv env, ApplicableResult applicableResult, QueryTarget target)
    {
        List<String> fusionFieldValues = term.getFusionFieldValue();
        if (fusionFieldValues != null)
        {
            List<String> initialSearchServerValues = new ArrayList<>();
            initialSearchServerValues.addAll(fusionFieldValues);
            term.setSearchServerFieldValue(initialSearchServerValues);
        }
        term.setWasMapped(true);
        String destinationFieldName = applicableResult.getDestinationFieldName();
        if (destinationFieldName != null)
        {
            term.setSearchServerFieldName(destinationFieldName);
        }

        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD, term.getFusionFieldName());
        newEnv.setBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_FIELD, destinationFieldName);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION, term.getFusionField());
        // don't apply operations on null value (empty list is OK)
        if (fusionFieldValues != null)
        {
            log.trace("Apply mapping [line: {}]: {}", getLocator().getLineNumber(), term);
            if (operations != null && operations.size() > 0)
            {
                for (Operation o : operations)
                {
                    try
                    {
                        o.applyAllQueryOperations(term, newEnv, target);
                        log.trace("Applied op {}:\n{}", o, term);
                    }
                    catch (UndeclaredFusionField e)
                    {
                        int line = locator.getLineNumber();
                        throw new UndeclaredFusionField("Fusion schema at line " + line + ": " + e.getMessage());
                    }
                }
            }
            else
            {
                // without any operations means "change"
                ChangeOperation changeOp = new ChangeOperation();
                changeOp.applyAllQueryOperations(term, newEnv, target);
                log.trace("Applied change op:\n{}", term);
            }
        }
    }

    /**
     * Is 'this' mapping applicable to the specified search server field? If true, then the field {@link
     * org.outermedia.solrfusion.configuration.ApplicableResult#destinationFieldName} is set to store the corresponding
     * fusion field name which is maybe constructed by a regular expression or wildcard.
     *
     * @param searchServerFieldName
     * @return a non null value if applicable else null
     */
    public ApplicableResult applicableToSearchServerField(String searchServerFieldName)
    {
        ApplicableResult result = mappingType.applicableToSearchServerField(searchServerFieldName, this);
        if (result != null)
        {
            result.setMapping(this);
        }
        return result;
    }

    public void applyResponseMappings(List<Term> terms, ScriptEnv env, FusionField fusionField,
        ApplicableResult applicableResult, ResponseTarget target)
    {
        for (Term t : terms)
        {
            applyResponseOperations(t, env, fusionField, applicableResult, target);
        }
    }

    /**
     * This method applies 'this' mapping to a given response term.
     *  @param term
     * @param env
     * @param fusionField
     * @param target
     */
    public void applyResponseOperations(Term term, ScriptEnv env, FusionField fusionField,
        ApplicableResult applicableResult, ResponseTarget target)
    {
        term.setFusionField(fusionField);

        // initialize term with mapped name and original search server value
        // don't map field when <om:add> is the operation,otherwise duplicate fields would be created
        // term.setFusionFieldName(specificFusionName);
        List<String> searchServerFieldValues = term.getSearchServerFieldValue();
        if (searchServerFieldValues != null)
        {
            List<String> initialFusionValues = new ArrayList<>();
            initialFusionValues.addAll(searchServerFieldValues);
            term.setFusionFieldValue(initialFusionValues);
        }
        List<Integer> searchServerFacets = term.getSearchServerFacetCount();
        if (searchServerFacets != null)
        {
            List<Integer> initialFacetCount = new ArrayList<>();
            initialFacetCount.addAll(searchServerFacets);
            term.setFusionFacetCount(initialFacetCount);
        }

        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD, applicableResult.getDestinationFieldName());
        newEnv.setBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_FIELD, term.getSearchServerFieldName());
        newEnv.setBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION, fusionField);
        newEnv.setBinding(ScriptEnv.ENV_IN_DOC_TERM, term);
        // don't apply operations on null value (empty list is OK)
        if (searchServerFieldValues != null)
        {
            if (operations != null && operations.size() > 0)
            {
                for (Operation o : operations)
                {
                    o.applyAllResponseOperations(term, newEnv, target);
                }
            }
            else
            {
                // without any operations means "change"
                ChangeOperation changeOp = new ChangeOperation();
                changeOp.applyAllResponseOperations(term, newEnv, target);
            }
        }
    }

    /**
     * Check attributes. Only the following combinations are valid: <oL> <li>name or</li> <li>fusion-name or</li>
     * <li>name and fusion-name or</li> <li>name-pattern, fusion-name-replacement, name-replacement and
     * fusion-name-pattern</li> </oL>
     *
     * @param u
     * @param parent
     * @throws UnmarshalException
     */
    protected void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException
    {
        searchServersNamePattern = trim(searchServersNamePattern);
        fusionNameReplacement = trim(fusionNameReplacement);
        searchServersNameReplacement = trim(searchServersNameReplacement);
        fusionNamePattern = trim(fusionNamePattern);
        searchServersName = trim(searchServersName);
        fusionName = trim(fusionName);

        boolean namePatSet = searchServersNamePattern != null;
        boolean fusionReplSet = fusionNameReplacement != null;
        boolean nameReplSet = searchServersNameReplacement != null;
        boolean fusionPatSet = fusionNamePattern != null;
        boolean nameWildCardSet = searchServersName != null && searchServersName.contains("*");
        boolean nameSet = searchServersName != null && !nameWildCardSet;
        boolean fusionWildCarSet = fusionName != null && fusionName.contains("*");
        boolean fusionSet = fusionName != null && !fusionWildCarSet;

        mappingType = MappingType.getMappingType(nameSet, fusionSet, namePatSet, fusionReplSet, nameReplSet,
            fusionPatSet, nameWildCardSet, fusionWildCarSet);
        int case1 = (mappingType == MappingType.EXACT_NAME_ONLY) ? 1 : 0;
        int case2 = (mappingType == MappingType.EXACT_FUSION_NAME_ONLY) ? 1 : 0;
        int case3 = (mappingType == MappingType.EXACT_NAME_AND_FUSION_NAME) ? 1 : 0;
        int case4 = (mappingType == MappingType.REG_EXP_ALL) ? 1 : 0;
        int case5 = (mappingType == MappingType.REG_EXP_NAME_ONLY) ? 1 : 0;
        int case6 = (mappingType == MappingType.REG_EXP_FUSION_NAME_ONLY) ? 1 : 0;
        int case7 = (mappingType == MappingType.WILDCARD_FUSION_NAME_ONLY) ? 1 : 0;
        int case8 = (mappingType == MappingType.WILDCARD_NAME_ONLY) ? 1 : 0;
        int case9 = (mappingType == MappingType.WILDCARD_NAME_AND_FUSION_NAME) ? 1 : 0;

        int trueCasesSum = case1 + case2 + case3 + case4 + case5 + case6 + case7 + case8 + case9;
        boolean noCaseIsTrue = trueCasesSum == 0;
        boolean moreThanOneCaseIsTrue = trueCasesSum > 1;

        if (noCaseIsTrue || moreThanOneCaseIsTrue)
        {
            log.error(
                "Invalid attribute combination: name={}, fusion-name={}, name-pattern={}, fusion-name-replacement={}, name-replacement={}, fusion-name-pattern={}",
                searchServersName, fusionName, searchServersNamePattern, fusionNameReplacement,
                searchServersNameReplacement, fusionNamePattern);
            throw new UnmarshalException("Invalid attribute combination");
        }
        if (case5 == 1 || case4 == 1)
        {
            searchServersNameRegExp = parseRegExp(searchServersNamePattern);
        }
        if (case6 == 1 || case4 == 1)
        {
            fusionNameRegExp = parseRegExp(fusionNamePattern);
        }

        if (operations != null)
        {
            for (Operation o : operations)
            {
                o.check(this);
            }
        }
    }

    protected String trim(String s)
    {
        if (s != null)
        {
            s = s.trim();
        }
        return s;
    }

    protected Pattern parseRegExp(String regExp) throws UnmarshalException
    {
        Pattern pat = null;
        try
        {
            pat = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        }
        catch (Exception e)
        {
            throw new UnmarshalException(e);
        }
        return pat;
    }

    public boolean isFusionFieldOnlyMapping()
    {
        return mappingType.isFusionFieldOnly();
    }

    public boolean isSearchServerFieldOnlyMapping()
    {
        return mappingType.isSearchServerFieldOnly();
    }

    public int getStartLineNumberInSchema()
    {
        int atLine = -1;
        if (locator != null)
        {
            atLine = locator.getLineNumber();
        }
        return atLine;
    }

    /**
     * Find all add query targets for the given level.
     *
     * @param level
     * @return a perhaps empty list
     */
    public List<Target> getAllAddQueryTargets(AddLevel level, QueryTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof AddOperation)
                {
                    if (level.equals(((AddOperation) o).getLevel()))
                    {
                        result.addAll(o.getQueryTargets(target));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Find all change query targets.
     *
     * @return a perhaps empty list
     */
    public List<Target> getAllChangeQueryTargets(QueryTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof ChangeOperation)
                {
                    result.addAll(o.getQueryTargets(target));
                }
            }
        }
        return result;
    }

    /**
     * Find all change response targets.
     *
     * @return a perhaps empty list
     */
    public List<Target> getAllChangeResponseTargets(ResponseTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof ChangeOperation)
                {
                    result.addAll(o.getResponseTargets(target));
                }
            }
        }
        return result;
    }

    public TargetsOfMapping getAllAddResponseTargets(ResponseTarget target)
    {
        TargetsOfMapping result = new TargetsOfMapping();
        result.setMappingsSearchServerFieldName(searchServersName);
        result.setMappingsFusionFieldName(fusionName);
        result.setMapping(this);
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof AddOperation)
                {
                    result.addAll(o.getResponseTargets(target));
                }
            }
        }
        return result;
    }

    public TargetsOfMapping getAllDropResponseTargets(ResponseTarget target)
    {
        TargetsOfMapping result = new TargetsOfMapping();
        result.setMappingsSearchServerFieldName(searchServersName);
        result.setMappingsFusionFieldName(fusionName);
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof DropOperation)
                {
                    result.addAll(o.getResponseTargets(target));
                }
            }
        }
        return result;
    }

    public List<Target> getAllDropQueryTargets(QueryTarget target)
    {
        List<Target> result = new ArrayList<>();
        if (operations != null)
        {
            for (Operation o : operations)
            {
                if (o instanceof DropOperation)
                {
                    result.addAll(o.getQueryTargets(target));
                }
            }
        }
        return result;
    }
}
