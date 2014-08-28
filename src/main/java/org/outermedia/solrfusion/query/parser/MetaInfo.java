package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
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
