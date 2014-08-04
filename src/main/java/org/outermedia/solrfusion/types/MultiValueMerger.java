package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 7/16/14.
 */
@ToString(callSuper = true)
@Getter
@Setter
@Slf4j
public class MultiValueMerger extends AbstractType
{
    private String range;

    private String separator;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        range = getConfigString("range", typeConfig, util);
        separator = getConfigString("separator", typeConfig, util, false);
        logBadConfiguration(range != null && separator != null, typeConfig);
    }

    @Override public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int limit = values.size();
        if (!"all".equals(range))
        {
            try
            {
                limit = Integer.parseInt(range);
            }
            catch (Exception e)
            {
                log.warn("Can't parse number '{}'. Using 'all' instead.", range);
            }
        }
        for (int i = 0; i < limit && i < values.size(); i++)
        {
            String s = values.get(i);
            if (s != null)
            {
                if (i > 0)
                {
                    sb.append(separator);
                }
                sb.append(s);
            }
        }
        if (sb.length() > 0)
        {
            result.add(sb.toString());
        }
        if (result.isEmpty())
        {
            result = null;
        }
        return result;
    }

    public static MultiValueMerger getInstance()
    {
        return new MultiValueMerger();
    }
}
