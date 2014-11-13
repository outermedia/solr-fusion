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
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.script.ScriptEngine;
import java.util.List;

/**
 * A Javascript Shell interpreter which evaluates expressions contained in the xml to process a field conversion.
 *
 * @author ballmann
 */

@ToString(callSuper = true, exclude = {"engine", "engineName"})
@Getter
@Setter
@Slf4j
public class Js extends AbstractType
{

    // either js, rhino, JavaScript, javascript, ECMAScript or ecmascript
    private String engineName = "js";

    private ScriptEngine engine;

    private String code;

    protected Js()
    {
        engine = getScriptEngine(engineName);
    }

    /**
     * The expected configuration is:
     * <pre>
     * {@code<script>
     *      // ... your code ...
     *  </script>
     * }
     * </pre>
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        code = getConfigString("script", typeConfig, util);
        logBadConfiguration(code != null, typeConfig);
        setReturnsFullQueries(true);
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env, ConversionDirection dir)
    {
        return applyScriptEngineCode(engine, code, values,facetDocCounts, env);
    }

    public static Js getInstance()
    {
        return new Js();
    }
}
