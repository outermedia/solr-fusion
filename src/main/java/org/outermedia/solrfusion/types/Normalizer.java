package org.outermedia.solrfusion.types;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ballmann on 8/20/14.
 */
@Slf4j
public class Normalizer extends AbstractType
{
    protected String startCharsToDel;
    protected boolean toLowerCase;

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        toLowerCase = "true".equals(getConfigString("to-lower-case", typeConfig, util, true));
        startCharsToDel = getConfigString("start-chars-to-del", typeConfig, util, false);
        // log.debug("Normalizer: trim={} toLowerCase={} startCharsToDel={}", trim, toLowerCase, startCharsToDel);
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        List<String> result = new ArrayList<>();
        for (String v : values)
        {
            if (v == null)
            {
                result.add(null);
            }
            else
            {
                if (toLowerCase)
                {
                    v = v.toLowerCase();
                }
                if (startCharsToDel != null)
                {
                    int at = 0;
                    while (at < v.length() && startCharsToDel.indexOf(v.charAt(at)) >= 0)
                    {
                        at++;
                    }
                    if (at >= v.length())
                    {
                        v = "";
                    }
                    else
                    {
                        v = v.substring(at);
                    }
                }
                result.add(v);
            }
        }
        return new TypeResult(result, facetWordCounts);
    }

    public static Normalizer getInstance()
    {
        return new Normalizer();
    }
}
