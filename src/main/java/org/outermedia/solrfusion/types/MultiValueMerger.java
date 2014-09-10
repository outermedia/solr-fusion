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
 * Flatten multiple values of one field to one value which is necessary when the destination field is a single value.
 *
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

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        List<String> newValues = new ArrayList<>();
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
            newValues.add(sb.toString());
        }
        if (!newValues.isEmpty())
        {
            if (newValues.size() != values.size() && facetWordCounts != null && facetWordCounts.size() > 0)
            {
                log.error("Script type MultiValueMerger merged values of search server field {} although facet word " +
                        "counts are present. Using original facet word count values.",
                    env.getBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_FIELD));
            }
            result = new TypeResult(newValues, facetWordCounts);
        }
        return result;
    }

    public static MultiValueMerger getInstance()
    {
        return new MultiValueMerger();
    }
}
