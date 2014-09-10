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
 * Data holder class to store one "response" configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "response", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
{
	"typeConfig"
})
@Getter
@Setter
@ToString(callSuper = true)
public class Response extends Target
{
    @XmlAttribute(required = false)
    private ResponseTarget target;

    public void initScriptEnv(ScriptEnv env)
    {
        ResponseTarget qt = target;
        if(qt == null) qt = ResponseTarget.ALL;
        env.setBinding(ScriptEnv.ENV_IN_RESPONSE_TARGET, qt);
    }
}
