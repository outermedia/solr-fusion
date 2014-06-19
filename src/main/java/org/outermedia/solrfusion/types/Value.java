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
@ToString(callSuper = true, exclude = {"engine", "engineName"})
@Getter
@Setter
public class Value extends AbstractType
{

    private List<String> values;

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
            log.error("Caught exception while parsing configuration: "
                    + typeConfig, e);
        }
        if (values.isEmpty())
        {
            values = null;
        }
    }

    @Override
    public List<String> apply(List<String> values, ScriptEnv env)
    {
        return this.values;
    }

    public static Value getInstance()
    {
        return new Value();
    }

}
