package org.outermedia.solrfusion.types;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;

/**
 * A Bean Shell interpreter which evaluates expressions contained in a file to process a field conversion.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Slf4j
public class BshFile extends Bsh
{

    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        String xpathStr = "//:file";
        try
        {
            String fileName = util.getValueOfXpath(xpathStr, typeConfig);
            setCode(FileUtils.readFileToString(new File(fileName)));
        }
        catch (Exception e)
        {
            log.error("Caught exception while parsing configuration: "
                    + typeConfig, e);
        }
    }

    public static BshFile getInstance()
    {
        return new BshFile();
    }
}
