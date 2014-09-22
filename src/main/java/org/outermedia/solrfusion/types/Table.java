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

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fusionToSearchServer table, embedded in the xml, is used to replace field values.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Slf4j
@Getter
@Setter
public class Table extends AbstractType
{
    private Map<String, String> fusionToSearchServer;
    private Map<String, String> searchServerToFusion;

    protected Table()
    {
        fusionToSearchServer = new HashMap<>();
        searchServerToFusion = new HashMap<>();
    }

    /**
     * The expected configuration is:
     * <pre>
     * {@code<entry>
     *      <value>v1</value>
     *      <fusion-value>w1</fusion-value>
     *  </entry>
     *  ...
     * }
     *  </pre>
     * At least one entry is expected.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        for (Element e : typeConfig)
        {
            List<Element> children = util.filterElements(e.getChildNodes());
            String fusionValue = null;
            String searchServerValue = null;
            for (Element me : children)
            {
                if ("value".equalsIgnoreCase(me.getLocalName()))
                {
                    searchServerValue = me.getTextContent();
                }
                if ("fusion-value".equalsIgnoreCase(me.getLocalName()))
                {
                    fusionValue = me.getTextContent();
                }
            }
            if (fusionValue != null && searchServerValue != null)
            {
                fusionToSearchServer.put(fusionValue, searchServerValue);
                searchServerToFusion.put(searchServerValue, fusionValue);
            }
            else
            {
                String xml = e.toString();
                try
                {
                    xml = util.xmlToString(e);
                }
                catch (TransformerException e1)
                {
                    // NOP
                }
                log.error("Couldn't parse configuration: {}", xml);
            }
        }
        if (fusionToSearchServer.isEmpty())
        {
            fusionToSearchServer = null;
        }
        if (searchServerToFusion.isEmpty())
        {
            searchServerToFusion = null;
        }
        logBadConfiguration(fusionToSearchServer != null && searchServerToFusion != null, typeConfig);
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        List<String> newValues = new ArrayList<>();
        Map<String, String> mapping = null;
        if (dir == ConversionDirection.SEARCH_TO_FUSION)
        {
            mapping = searchServerToFusion;
        }
        else if (dir == ConversionDirection.FUSION_TO_SEARCH)
        {
            mapping = fusionToSearchServer;
        }
        else
        {
            throw new RuntimeException("Unsupported conversion direction: " + dir);
        }
        result = new TypeResult(newValues, facetDocCounts);
        for (String v : values)
        {
            if (v == null)
            {
                newValues.add(null);
            }
            else
            {
                String nv = mapping.get(v);
                if (nv != null)
                {
                    newValues.add(nv);
                }
                else
                {
                    log.warn("Can't convert '{}' {}. Please fix your mapping.", v, dir);
                }
            }
        }
        if (newValues.isEmpty())
        {
            result = null;
        }
        return result;
    }

    public static Table getInstance()
    {
        return new Table();
    }
}
