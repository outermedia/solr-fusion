package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Set (overwrite) a field's value with one or more static values.
 *
 * @author ballmann
 */

@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
public class Value extends AbstractType
{

    private List<String> values;

    /**
     * The expected configuration is:
     * <pre>
     * {@code<value>val1</value>
     *  ...
     *  <value>valn</value>
     * }
     *  </pre>
     * n is &gt;= 1.
     *
     * @param typeConfig a list of XML elements
     * @param util       helper which simplifies to apply xpaths
     */
    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        values = new ArrayList<>();

        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        String xpathStr = "//:value";
        try
        {
            List<Element> elements = util.xpathElements(xpathStr, typeConfig);
            for (Element e : elements)
            {
                values.add(e.getTextContent());
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: " + elementListToString(typeConfig), e);
        }
        if (values.isEmpty())
        {
            values = null;
        }
        logBadConfiguration(values != null && !values.isEmpty(), typeConfig);
    }

    @Override
    public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = null;
        if (this.values != null)
        {
            result = new ArrayList<>();
            result.addAll(this.values);
        }
        ResponseTarget responseTarget = null;
        if (env != null)
        {
            responseTarget = (ResponseTarget) env.getBinding(ScriptEnv.ENV_IN_RESPONSE_TARGET);
        }
        if (responseTarget == ResponseTarget.FACET && result != null)
        {
            facetWordCounts = new ArrayList<>();
            for (int i = 0; i < result.size(); i++)
            {
                facetWordCounts.add(1);
            }
        }
        return new TypeResult(this.values, facetWordCounts);
    }

    public static Value getInstance()
    {
        return new Value();
    }

}
