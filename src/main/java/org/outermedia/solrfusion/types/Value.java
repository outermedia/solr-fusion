package org.outermedia.solrfusion.types;

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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Set (overwrite) a field's value with one or more static values.
 *
 * @author ballmann
 */

@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
public class Value extends AbstractType
{

    private List<String> values;

    /**
     * The expected configuration is:
     * <pre>
     * {@code<value>val1</value>
     *  ...
     *  <value>valn</value>
     * }
     *  </pre>
     * n is &gt;= 1.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        values = new ArrayList<>();

        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        String xpathStr = "//:value";
        try
        {
            List<Element> elements = util.xpathElements(xpathStr, typeConfig);
            for (Element e : elements)
            {
                values.add(e.getTextContent());
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: " + elementListToString(typeConfig), e);
        }
        if (values.isEmpty())
        {
            values = null;
        }
        logBadConfiguration(values != null && !values.isEmpty(), typeConfig);
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = null;
        if (this.values != null)
        {
            result = new ArrayList<>();
            result.addAll(this.values);
        }
        ResponseTarget responseTarget = null;
        if (env != null)
        {
            responseTarget = (ResponseTarget) env.getBinding(ScriptEnv.ENV_IN_RESPONSE_TARGET);
        }
        if (responseTarget == ResponseTarget.FACET && result != null)
        {
            facetDocCounts = new ArrayList<>();
            for (int i = 0; i < result.size(); i++)
            {
                facetDocCounts.add(1);
            }
        }
        return new TypeResult(this.values, facetDocCounts);
    }

    public static Value getInstance()
    {
        return new Value();
    }

}
