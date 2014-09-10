package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Data holder class to store the configuration of one "query".
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "query", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"typeConfig"
})
@Getter
@Setter
@ToString(callSuper = true)
public class Query extends Target
{
    @XmlAttribute(required = false)
    private QueryTarget target;

    public void initScriptEnv(ScriptEnv env)
    {
        QueryTarget qt = target;
        if(qt == null) qt = QueryTarget.ALL;
        env.setBinding(ScriptEnv.ENV_IN_QUERY_TARGET, qt);
    }
}
