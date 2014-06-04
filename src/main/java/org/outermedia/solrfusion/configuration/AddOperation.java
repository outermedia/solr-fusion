package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.parser.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Data holder to store add operation configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "targets"
        })
@Getter
@Setter
@ToString(callSuper = true)
public class AddOperation extends Operation
{

    @Override
    protected void applyOneQueryOperation(Term term, ScriptEnv env, Target t)
    {
        super.applyOneQueryOperation(term, env, t);
        term.addNewSearchServerQuery(env.getStringBinding(ScriptEnv.ENV_SEARCH_SERVER_FIELD), term.getSearchServerFieldValue());
    }
}
