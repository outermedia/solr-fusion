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

import java.util.Map;

/**
 * This class stores {!key[=val] (key=val)*} of a Solr query.
 * 
 * Created by ballmann on 8/8/14.
 */
@Getter
@Setter
@ToString
public class MetaInfo implements MetaParamsVisitor<StringBuilder>
{
    private String name;
    private String value;
    private MetaParams fusionParams;
    private MetaParams searchServerParams;

    public static final String DISMAX_PARSER = "dismax";

    public MetaInfo()
    {
        fusionParams = new MetaParams();
    }

    public void addFusionEntry(String key, String value)
    {
        fusionParams.addEntry(key, value);
    }

    public void addSearchServerEntry(String key, String value)
    {
        searchServerParams.addEntry(key, value);
    }

    public Map<String, String> getFusionParameterMap()
    {
        return fusionParams.getKeyValue();
    }

    public Map<String, String> getSearchServerParameterMap()
    {
        Map<String, String> result = null;
        if (searchServerParams != null)
        {
            result = searchServerParams.getKeyValue();
        }
        return result;
    }

    public void accept(MetaParamsVisitor<MetaParams> visitor)
    {
        searchServerParams = new MetaParams();
        fusionParams.accept(visitor, searchServerParams);
    }

    public void resetQuery()
    {
        searchServerParams = null;
    }

    public void buildSearchServerQueryString(StringBuilder builder)
    {
        StringBuilder sb = new StringBuilder();
        if (name != null)
        {
            sb.append(name);
            if (value != null)
            {
                sb.append('=');
                if (value.contains(" "))
                {
                    sb.append('\"');
                }
                sb.append(value);
                if (value.contains(" "))
                {
                    sb.append('\"');
                }
            }
        }
        if (searchServerParams != null && !searchServerParams.isEmpty())
        {
            searchServerParams.accept(this, sb);
        }
        if (sb.length() > 0)
        {
            builder.append("{!");
            builder.append(sb);
            builder.append("}");
        }
    }

    @Override public void visitEntry(String key, String value, StringBuilder builder)
    {
        if (builder.length() > 0)
        {
            builder.append(" ");
        }
        builder.append(key);
        builder.append('=');
        if (value.contains(" "))
        {
            builder.append('\"');
        }
        builder.append(value);
        if (value.contains(" "))
        {
            builder.append('\"');
        }
    }

    public boolean isDismax()
    {
        return DISMAX_PARSER.equals(name);
    }

    public MetaInfo shallowClone()
    {
        MetaInfo result = new MetaInfo();
        result.setName(name);
        result.setValue(value);
        if (searchServerParams != null)
        {
            result.setSearchServerParams(searchServerParams.shallowClone());
        }
        if (fusionParams != null)
        {
            result.setFusionParams(fusionParams.shallowClone());
        }
        return result;
    }
}
