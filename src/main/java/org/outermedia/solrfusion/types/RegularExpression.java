package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regular expression which matches a pattern and applies a replacement in order to process a field conversion.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Getter
@Setter
@Slf4j
public class RegularExpression extends AbstractType
{

    private Pattern pattern;
    private String replacement;

    protected RegularExpression()
    {
    }

    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        try
        {
            String xpathStr = "//:pattern";
            String patternStr = util.getValueOfXpath(xpathStr, typeConfig);
            pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: "
                    + typeConfig, e);
        }

        try
        {
            String xpathStr = "//:replacement";
            replacement = util.getValueOfXpath(xpathStr, typeConfig);
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: "
                    + typeConfig, e);
        }

    }

    @Override
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = new ArrayList<>();
        for (String v : values)
        {
            Matcher matcher = pattern.matcher(v);
            String rv = matcher.replaceAll(replacement);
            result.add(rv);
        }
        if (values.isEmpty())
        {
            result = null;
        }
        return result;
    }

    public static RegularExpression getInstance()
    {
        return new RegularExpression();
    }
}
