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
 * A Bean Shell interpreter which evaluates expressions contained in the xml to process a field conversion.
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
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
//        log.debug("VALUE {} sees fusion-value={} search-server-value={}", this.values,
//            env.getBinding(ScriptEnv.ENV_FUSION_VALUE), env.getBinding(ScriptEnv.ENV_SEARCH_SERVER_VALUE));
        return this.values;
    }

    public static Value getInstance()
    {
        return new Value();
    }

}
