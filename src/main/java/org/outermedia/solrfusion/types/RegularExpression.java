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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regular expression which matches a pattern and applies a replacement in order to process a field conversion. All
 * occurrences are replaced.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Getter
@Setter
@Slf4j
public class RegularExpression extends AbstractType
{

    private Pattern pattern;
    private String replacement;

    protected RegularExpression()
    {
    }

    /**
     * The expected configuration is:
     * <pre>
     * {@code<pattern>regular.expression</pattern>
     *  <replacement>text</replacement>
     * }
     *  </pre>
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        try
        {
            String patternStr = getConfigString("pattern", typeConfig, util);
            pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: " + typeConfig, e);
        }

        replacement = getConfigString("replacement", typeConfig, util);

        logBadConfiguration(pattern != null && replacement != null, typeConfig);
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        List<String> newValues = new ArrayList<>();
        TypeResult result = new TypeResult(newValues, facetWordCounts);
        for (String v : values)
        {
            if (v != null)
            {
                Matcher matcher = pattern.matcher(v);
                String rv = matcher.replaceAll(replacement);
                newValues.add(rv);
            }
            else
            {
                newValues.add(null);
            }
        }
        if (values.isEmpty())
        {
            result = null;
        }
        return result;
    }

    public static RegularExpression getInstance()
    {
        return new RegularExpression();
    }
}
