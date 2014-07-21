package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

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

    @XmlAttribute(required = true)
    private AddLevel level;

    @Override
    protected void applyOneQueryOperation(Term term, ScriptEnv env, Target t)
    {
        super.applyOneQueryOperation(term, env, t);
        term.addNewSearchServerQuery(level == AddLevel.INSIDE, term.getSearchServerFieldValue(), env.getConfiguration(),
            env.getLocale());
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
        super.applyOneResponseOperation(term, new ScriptEnv(), t);
        if (isNew)
        {
            doc.addFusionField(fusionFieldName, fusionField, term.getFusionFieldValue());
            added = true;
        }
        return added;
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
