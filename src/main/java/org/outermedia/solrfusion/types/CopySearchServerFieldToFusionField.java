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
 * Internally used default ScriptType for <pre>{@code<om:add><om:response>}</pre>.
 *
 * Created by ballmann on 8/1/14.
 */
@Slf4j
public class CopySearchServerFieldToFusionField extends AbstractType
{
    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        // NOP
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        List<String> newValues = null;
        if (values != null)
        {
            newValues = new ArrayList<>();
            newValues.addAll(values);
            result = new TypeResult(newValues, facetDocCounts);
        }
        return result;
    }

    public static CopySearchServerFieldToFusionField getInstance()
    {
        return new CopySearchServerFieldToFusionField();
    }
}
