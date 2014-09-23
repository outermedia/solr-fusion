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
public class SetFacetDocCount extends AbstractType
{

    private Integer docCount;
    private boolean useValue;
    private boolean useTotalFoundNr;

    /**
     * The expected configuration is e.g.:
     * <pre>
     * {@code <value>5</value
     * } or
     * {@code <total-found-nr />}
     *  </pre>
     * Either provide a static doc count value or use the total number of found documents of a Solr response.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        useValue = false;
        useTotalFoundNr = false;
        try
        {
            String docCountStr = util.getValueOfXpath("//:value", typeConfig, true);
            if (docCountStr != null)
            {
                docCount = Integer.valueOf(docCountStr);
                useValue = true;
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while getting <value>.", e);
        }
        try
        {
            useTotalFoundNr = util.xpathPresent("//:total-found-nr", typeConfig);
            useValue = false;
        }
        catch (Exception e)
        {
            log.error("Caught exception while getting <value>.", e);
        }
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env, ConversionDirection dir)
    {
        Integer v = null;
        if (useValue)
        {
            v = docCount;
        }
        if (useTotalFoundNr)
        {
            v = (Integer) env.getBinding(ScriptEnv.ENV_IN_TOTAL_DOC_NR);
        }
        facetDocCounts.set(0, v);
        return new TypeResult(values, facetDocCounts);
    }

    public static SetFacetDocCount getInstance()
    {
        return new SetFacetDocCount();
    }

}
