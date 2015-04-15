package org.outermedia.solrfusion;

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

import com.google.common.collect.HashMultimap;
import lombok.EqualsAndHashCode;
import org.outermedia.solrfusion.query.SolrFusionRequestParams;

import java.util.*;

/**
 * A helper class which is used to store Solr HTTP request parameters which might have multiple values.
 * <p/>
 * Created by ballmann on 8/8/14.
 */
@EqualsAndHashCode
public class Multimap<V>
{
    protected HashMultimap<String, V> map;


    public Multimap()
    {
        map = HashMultimap.create();
    }

    public Collection<V> get(String key)
    {
        return map.get(key);
    }

    public Collection<V> get(SolrFusionRequestParams sp)
    {
        return get(sp.getRequestParamName());
    }

    public void put(String key, V val)
    {
        map.put(key, val);
    }

    public void put(SolrFusionRequestParams key, V val)
    {
        put(key.getRequestParamName(), val);
    }

    public void put(SolrFusionRequestParams key, SolrFusionRequestParam sp)
    {
        String value = sp.getValue();
        if (value != null)
        {
            put(key, (V) value);
        }
    }

    public void set(SolrFusionRequestParams key, V val)
    {
        set(key.getRequestParamName(), val);
    }

    public void set(String key, V val)
    {
        map.removeAll(key);
        put(key, val);
    }

    public V getFirst(String key)
    {
        V result = null;
        Collection<V> values = get(key);
        if (values != null && !values.isEmpty())
        {
            result = values.iterator().next();
        }
        return result;
    }

    public V getFirst(SolrFusionRequestParams key)
    {
        return getFirst(key.getRequestParamName());
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key : map.keySet())
        {
            sb.append("\n\t");
            if (sb.length() > 2)
            {
                sb.append(",");
            }
            sb.append(key);
            sb.append(": [");
            Set<V> values = map.get(key);
            if (values != null)
            {
                int count = 0;
                for (V v : values)
                {
                    if (count > 0)
                    {
                        sb.append(", ");
                    }
                    if (v instanceof String)
                    {
                        sb.append('"');
                    }
                    if (v == null)
                    {
                        sb.append("null");
                    }
                    else
                    {
                        sb.append(v.toString());
                    }
                    if (v instanceof String)
                    {
                        sb.append('"');
                    }
                    count++;
                }
            }
            sb.append("]");
        }
        sb.append("\n}");
        return sb.toString();
    }

    public List<Map.Entry<String, V>> filterBy(SolrFusionRequestParams field)
    {
        List<Map.Entry<String, V>> result = new ArrayList<>();
        for (Map.Entry<String, V> e : map.entries())
        {
            String key = e.getKey();
            if (field.matches(key) != null)
            {
                result.add(e);
            }
        }
        return result;
    }

    public void delete(String key)
    {
        map.removeAll(key);
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public Set<String> allKeys()
    {
        return map.keySet();
    }
}
