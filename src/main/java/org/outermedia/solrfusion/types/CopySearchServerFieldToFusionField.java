package org.outermedia.solrfusion.types;

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

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        List<String> newValues = null;
        if (values != null)
        {
            newValues = new ArrayList<>();
            newValues.addAll(values);
            result = new TypeResult(newValues, facetWordCounts);
        }
        return result;
    }

    public static CopySearchServerFieldToFusionField getInstance()
    {
        return new CopySearchServerFieldToFusionField();
    }
}
