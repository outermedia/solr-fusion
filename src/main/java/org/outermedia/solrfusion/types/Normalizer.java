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

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Especially for SolrFusion's sorting it is necessary to use comparable strings.
 *
 * Created by ballmann on 8/20/14.
 */
@Slf4j
public class Normalizer extends AbstractType
{
    protected String startCharsToDel;
    protected boolean toLowerCase;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        toLowerCase = "true".equals(getConfigString("to-lower-case", typeConfig, util, true));
        startCharsToDel = getConfigString("start-chars-to-del", typeConfig, util, false);
        // log.debug("Normalizer: trim={} toLowerCase={} startCharsToDel={}", trim, toLowerCase, startCharsToDel);
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        List<String> result = new ArrayList<>();
        for (String v : values)
        {
            if (v == null)
            {
                result.add(null);
            }
            else
            {
                if (toLowerCase)
                {
                    v = v.toLowerCase();
                }
                if (startCharsToDel != null)
                {
                    int at = 0;
                    while (at < v.length() && startCharsToDel.indexOf(v.charAt(at)) >= 0)
                    {
                        at++;
                    }
                    if (at >= v.length())
                    {
                        v = "";
                    }
                    else
                    {
                        v = v.substring(at);
                    }
                }
                result.add(v);
            }
        }
        return new TypeResult(result, facetDocCounts, isReturnsFullQueries());
    }

    public static Normalizer getInstance()
    {
        return new Normalizer();
    }
}
