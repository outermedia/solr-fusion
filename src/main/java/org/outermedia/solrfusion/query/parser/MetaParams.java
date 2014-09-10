package org.outermedia.solrfusion.query.parser;

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
