package org.outermedia.solrfusion.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.types.AbstractType;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Abstract data holder class to store the configuration of common attributes of a &lt;query&gt;, &lt;response&gt; or
 * &lt;query-response&gt;.
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
    @XmlIDREF @XmlAttribute(name = "type", required = false)
    private ScriptType type;

    /**
     * Only used if a field is split/joined.
     */
    @XmlAttribute(name = "name", required = false)
    private String name;

    /**
     * Only used if a field is split/joined.
     */
    @XmlAttribute(name = "fusion-name", required = false)
    private String fusionName;

    @XmlAnyElement
    private List<Element> typeConfig;

    @XmlTransient @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Util util;

    public Target()
    {
        util = new Util();
    }

    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = null;
        if (type != null)
        {
            try
            {
                AbstractType typeImpl = (AbstractType) type.getInstance();
                if (typeImpl != null)
                {
                    typeImpl.passArguments(typeConfig, util);
                    result = typeImpl.apply(values, env, dir);
                }
                else
                {
                    log.error("Can't apply script type {}, because getting instance failed.", type);
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
