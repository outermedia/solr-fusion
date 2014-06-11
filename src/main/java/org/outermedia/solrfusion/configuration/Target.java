package org.outermedia.solrfusion.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.types.AbstractType;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Abstract data holder class to store the configuration of common attributes of
 * a &lt;query&gt;, &lt;response&gt; or &lt;query-response&gt;.
 *
 * @author ballmann
 */

@XmlTransient
@Getter
@Setter
@ToString(exclude = "util")
@Slf4j
public abstract class Target
{
    @XmlIDREF
    @XmlAttribute(name = "type", required = false)
    private ScriptType type;

    @XmlAttribute(name = "name", required = false)
    private String name;

    @XmlAttribute(name = "fusion-name", required = false)
    private String fusionName;

    @XmlAnyElement
    private List<Element> typeConfig;

    @XmlTransient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Util util;

    public Target()
    {
        util = new Util();
    }

    public String apply(ScriptEnv env)
    {
        String result = null;
        if (type != null)
        {
            try
            {
                AbstractType typeImpl = (AbstractType) type.getInstance();
                if (typeImpl != null)
                {
                    typeImpl.passArguments(typeConfig, util);
                    result = typeImpl.apply(env);
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while applying " + type + " to " + this, e);
            }
        }
        return result;
    }
}
