package org.outermedia.solrfusion.query.parser;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper class to store {! ... key=val ...}.
 *
 * Created by ballmann on 8/22/14.
 */
@Getter
@Setter
@ToString
public class MetaParams
{
    protected Map<String, String> keyValue;

    public MetaParams()
    {
        keyValue = new LinkedHashMap<>();
    }

    public void addEntry(String key, String value)
    {
        keyValue.put(key, value);
    }

    public <C> void accept(MetaParamsVisitor visitor, C context)
    {
        for(Map.Entry<String, String> entry : keyValue.entrySet())
        {
            visitor.visitEntry(entry.getKey(), entry.getValue(), context);
        }
    }

    public boolean isEmpty()
    {
        return keyValue.isEmpty();
    }

    public MetaParams shallowClone()
    {
        MetaParams result = new MetaParams();
        for(Map.Entry<String, String> entry : keyValue.entrySet())
        {
           result.addEntry(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
