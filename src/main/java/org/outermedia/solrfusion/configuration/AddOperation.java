package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Locale;

/**
 * Data holder to store add operation configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder = {"targets"})
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class AddOperation extends Operation
{
    // responses don't need a level
    @XmlAttribute(required = false)
    private AddLevel level;

    @Override
    protected void applyOneQueryOperation(Term term, ScriptEnv env, Target t)
    {
        // outside added queries will be handled later
        if (level == AddLevel.INSIDE)
        {
            // the super call would overwrite the search server field value which is initialized with the fusion field
            // value either the field is dropped or changed
            // super.applyOneQueryOperation(term, env, t);
            List<String> newSearchServerValue = t.apply(term.getSearchServerFieldValue(), env,
                ConversionDirection.FUSION_TO_SEARCH);
            term.addNewSearchServerQuery(true, newSearchServerValue, env.getConfiguration(), env.getLocale());
        }
    }

    @Override
    protected void applyOneResponseOperation(Term term, ScriptEnv env, Target t)
    {
        // NOP will be done later see addToResponse() below
    }

    public boolean addToResponse(Document doc, String fusionFieldName, FusionField fusionField, Target t)
    {
        boolean added = false;
        Term term = doc.getFieldTermByFusionName(fusionFieldName);
        boolean isNew = false;
        if (term == null)
        {
            term = Term.newFusionTerm(fusionFieldName);
            isNew = true;
        }
        ScriptEnv env = getResponseScriptEnv(term, new ScriptEnv());
        super.applyOneResponseOperation(term, env, t);
        if (isNew)
        {
            doc.addFusionField(fusionFieldName, fusionField, term.getFusionFieldValue());
            added = true;
        }
        return added;
    }

    public List<org.outermedia.solrfusion.query.parser.Query> addToQuery(Configuration configuration,
        String searchServerFieldName, Target t, Locale locale)
    {
        Term term = Term.newSearchServerTerm(searchServerFieldName);
        ScriptEnv env = getQueryScriptEnv(term, new ScriptEnv());
        super.applyOneQueryOperation(term, env, t);
        term.addNewSearchServerQuery(false, term.getSearchServerFieldValue(), configuration, locale);
        List<org.outermedia.solrfusion.query.parser.Query> newQueries = term.getNewQueryTerms();
        return newQueries;
    }

    @Override
    public void check(FieldMapping fieldMapping) throws UnmarshalException
    {
        String msg = null;

        List<Target> responses = getResponseTargets();
        if (fieldMapping.getFusionName() == null)
        {
            if (responses.size() > 0)
            {
                msg = "Please specify a field for attribute 'fusion-name' in order to add something to a response.";
            }
        }
        for (Target t : responses)
        {
            if (t.getType() == null)
            {
                msg = "Please specify a value for the 'type' attribute in the '<om:add>' '<om:response>' operation.";
            }
        }

        List<Target> queries = getQueryTargets();
        if (fieldMapping.getSearchServersName() == null)
        {
            if (queries.size() > 0)
            {
                msg = "Please specify a field for attribute 'name' in order to add something to a query.";
            }
        }
        for (Target t : queries)
        {
            if (t.getType() == null)
            {
                msg = "Please specify a value for the 'type' attribute in the '<om:add>' '<om:query>' operation.";
            }
        }

        if (msg != null)
        {
            msg = "In fusion schema at line " + fieldMapping.geStartLineNumberInSchema() + ": " + msg;
            log.error(msg);
            throw new UnmarshalException(msg);
        }
    }
}
