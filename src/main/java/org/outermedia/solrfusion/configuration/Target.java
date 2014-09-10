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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.types.AbstractType;
import org.outermedia.solrfusion.types.ConversionDirection;
import org.outermedia.solrfusion.types.ScriptEnv;
import org.outermedia.solrfusion.types.TypeResult;
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

    public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env, ConversionDirection dir)
    {
        TypeResult result = null;
        if (type != null)
        {
            try
            {
                initScriptEnv(env);
                AbstractType typeImpl = type.getInstance();
                if (typeImpl != null)
                {
                    typeImpl.passArguments(typeConfig, util);
                    result = typeImpl.apply(values, facetWordCounts, env, dir);
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

    public void initScriptEnv(ScriptEnv env)
    {
        // NOP, for subclasses
    }
}
