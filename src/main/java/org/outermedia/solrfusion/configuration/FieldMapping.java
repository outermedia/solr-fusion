package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Data holder class to store one field mapping configuration.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fieldMapping", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "operations"
        })
@Getter
@Setter
@ToString(exclude = {"searchServersNameRegExp", "fusionNameRegExp"})
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
    private MatchType matchType;

    /**
     * This field is set, when {@link #applicableToSearchServerField(String)} has been called.
     */
    @XmlTransient
    private String specificFusionName;

    /**
     * This field is set, when {@link #applicableToFusionField(String)} has been called.
     */
    @XmlTransient
    private String specificSearchServerName;

    @XmlElements(value =
            {
                    @XmlElement(name = "add", type = AddOperation.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "drop", type = DropOperation.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/"),
                    @XmlElement(name = "change", type = ChangeOperation.class,
                            namespace = "http://solrfusion.outermedia.org/configuration/")
            })
    private List<Operation> operations;

    /**
     * Is 'this' mapping applicable to the specified fusion field? If true, then the field {@link
     * #specificSearchServerName} is set to store the corresponding search server field name which is maybe constructed
     * by a regular expression or wildcard.
     *
     * @param fusionFieldName
     * @return true if applicable else false
     */
    public boolean applicableToFusionField(String fusionFieldName)
    {
        ApplicableResult matchResult = matchType.applicableToFusionField(fusionFieldName, this);
        if (matchResult != null)
        {
            specificSearchServerName = matchResult.getDestinationFieldName();
        }
        return matchResult != null;
    }

    /**
     * This method applies 'this' mapping to a given query term. Please note that a previous call of {@link
     * #applicableToFusionField(String)} is expected, because {@link #specificSearchServerName} is then set.
     *
     * @param term
     * @param env
     */
    public void applyQueryMappings(Term term, ScriptEnv env)
    {
        // initialize term with mapped name and original fusion value
        term.setSearchServerFieldName(specificSearchServerName);
        term.setSearchServerFieldValue(term.getFusionFieldValue());
        term.setWasMapped(true);

        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_FIELD, term.getFusionFieldName());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_FIELD, term.getSearchServerFieldName());
        newEnv.setBinding(ScriptEnv.ENV_FUSION_FIELD_DECLARATION, term.getFusionField());
        if (operations != null)
        {
            for (Operation o : operations)
            {
                o.applyAllQueryOperations(term, newEnv);
            }
        }
    }

    /**
     * Is 'this' mapping applicable to the specified search server field? If true, then the field {@link
     * #specificFusionName} is set to store the corresponding fusion field name which is maybe constructed by a regular
     * expression or wildcard.
     *
     * @param searchServerFieldName
     * @return true if applicable else false
     */
    public boolean applicableToSearchServerField(String searchServerFieldName)
    {
        ApplicableResult matchResult = matchType.applicableToSearchServerField(searchServerFieldName, this);
        if (matchResult != null)
        {
            specificFusionName = matchResult.getDestinationFieldName();
        }
        return matchResult != null;
    }

    public void applyResponseMappings(List<Term> terms, ScriptEnv env, FusionField fusionField)
    {
        for (Term t : terms)
        {
            applyResponseMappings(t, env, fusionField);
        }
    }

    /**
     * This method applies 'this' mapping to a given response term. Please note that a previous call of {@link
     * #applicableToSearchServerField(String)} is expected, because {@link #specificFusionName} is then set.
     *
     * @param term
     * @param env
     * @param fusionField
     */
    public void applyResponseMappings(Term term, ScriptEnv env, FusionField fusionField)
    {
        term.setFusionField(fusionField);

        // initialize term with mapped name and original search server value
        term.setFusionFieldName(specificFusionName);
        term.setFusionFieldValue(term.getSearchServerFieldValue());
        term.setWasMapped(true);

        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_FUSION_FIELD, term.getFusionFieldName());
        newEnv.setBinding(ScriptEnv.ENV_SEARCH_SERVER_FIELD, term.getSearchServerFieldName());
        newEnv.setBinding(ScriptEnv.ENV_FUSION_FIELD_DECLARATION, term.getFusionField());
        if (operations != null)
        {
            for (Operation o : operations)
            {
                o.applyAllResponseOperations(term, newEnv);
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
    protected void afterUnmarshal(Unmarshaller u, Object parent)
            throws UnmarshalException
    {
        boolean nameSet = searchServersName != null;
        boolean fusionSet = fusionName != null;
        boolean namePatSet = searchServersNamePattern != null;
        boolean fusionReplSet = fusionNameReplacement != null;
        boolean nameReplSet = searchServersNameReplacement != null;
        boolean fusionPatSet = fusionNamePattern != null;

        int case1 = (nameSet && !fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet) ? 1 : 0;
        int case2 = (!nameSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet) ? 1 : 0;
        int case3 = (nameSet && fusionSet && !namePatSet && !fusionReplSet && !nameReplSet && !fusionPatSet) ? 1 : 0;
        int case4 = (!nameSet && !fusionSet && namePatSet && fusionReplSet && nameReplSet && fusionPatSet) ? 1 : 0;

        int trueCasesSum = case1 + case2 + case3 + case4;
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
        if (case1 == 1 || case2 == 1 || case3 == 1)
        {
            matchType = MatchType.LITERAL;
        }
        else if (case4 == 1)
        {
            matchType = MatchType.REG_EXP;
            searchServersNameRegExp = parseRegExp(searchServersNamePattern);
            fusionNameRegExp = parseRegExp(fusionNamePattern);
        }
        else
        {
            throw new UnmarshalException("Unknown match type");
        }
    }

    protected Pattern parseRegExp(String regExp) throws UnmarshalException
    {
        Pattern pat = null;
        try
        {
            pat = Pattern.compile(regExp);
        }
        catch (Exception e)
        {
            throw new UnmarshalException(e);
        }
        return pat;
    }
}
