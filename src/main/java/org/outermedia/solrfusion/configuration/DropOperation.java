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
 * Data holder to store drop operation configurations.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dropOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"targets"
})
@Getter
@Setter
@ToString(callSuper = true)
public class DropOperation extends Operation
{
    @Override
    public void applyAllQueryOperations(Term term, ScriptEnv env)
    {
        term.setRemoved(true);
    }
}
