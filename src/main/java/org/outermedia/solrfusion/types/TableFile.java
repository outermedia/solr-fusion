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

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A mapping table, contained in a file, is used to replace field values.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Slf4j
public class TableFile extends Table
{

    /**
     * The expected configuration is:
     * <pre>
     * {@code<file>path-to.xml</file>}
     * </pre>
     * The xml's expected format is decribed in {@link org.outermedia.solrfusion.types.Table#passArguments(java.util.List,
     * org.outermedia.solrfusion.configuration.Util)}. You can use any root element you want.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        try
        {
            String fileName = getConfigString("file", typeConfig, util);
            Element rootElem = util.parseXmlFromFile(fileName);
            List<Element> entries = util.filterElements(rootElem.getChildNodes());
            super.passArguments(entries, util);
        }
        catch (Exception e)
        {
            setFusionToSearchServer(null);
            setSearchServerToFusion(null);
            log.error("Caught exception while parsing configuration: "
                    + elementListToString(typeConfig), e);
        }
        logBadConfiguration(getFusionToSearchServer() != null && getSearchServerToFusion() != null, typeConfig);
    }

    public static TableFile getInstance()
    {
        return new TableFile();
    }
}
