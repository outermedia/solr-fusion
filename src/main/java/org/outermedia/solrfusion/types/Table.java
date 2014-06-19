package org.outermedia.solrfusion.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Util;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fusionToSearchServer table, embedded in the xml, is used to replace field values.
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Slf4j
@Getter
@Setter
public class Table extends AbstractType
{
    private Map<String, String> fusionToSearchServer;
    private Map<String, String> searchServerToFusion;

    protected Table()
    {
        fusionToSearchServer = new HashMap<>();
        searchServerToFusion = new HashMap<>();
    }

    @Override
    public void passArguments(List<Element> typeConfig, Util util)
    {
        for (Element e : typeConfig)
        {
            List<Element> children = util.filterElements(e.getChildNodes());
            String fusionValue = null;
            String searchServerValue = null;
            for (Element me : children)
            {
                if ("value".equalsIgnoreCase(me.getLocalName()))
                {
                    searchServerValue = me.getTextContent();
                }
                if ("fusion-value".equalsIgnoreCase(me.getLocalName()))
                {
                    fusionValue = me.getTextContent();
                }
            }
            if (fusionValue != null && searchServerValue != null)
            {
                fusionToSearchServer.put(fusionValue, searchServerValue);
                searchServerToFusion.put(searchServerValue, fusionValue);
            }
            else
            {
                String xml = e.toString();
                try
                {
                    xml = util.xmlToString(e);
                }
                catch (TransformerException e1)
                {
                    // NOP
                }
                log.error("Couldn't parse configuration: " + xml);
            }
        }
    }

    @Override
    public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        List<String> result = new ArrayList<>();
        Map<String, String> mapping = null;
        if (dir == ConversionDirection.SEARCH_TO_FUSION)
        {
            mapping = searchServerToFusion;
        }
        else if (dir == ConversionDirection.FUSION_TO_SEARCH)
        {
            mapping = fusionToSearchServer;
        }
        else
        {
            throw new RuntimeException("Unsupported conversion direction: " + dir);
        }
        for (String v : values)
        {
            String nv = mapping.get(v);
            if (nv != null)
            {
                result.add(nv);
            }
            else
            {
                log.warn("Can't convert '{}' {}. Please fix your mapping.", v, dir);
            }
        }
        if (result.isEmpty())
        {
            result = null;
        }
        return result;
    }

    public static Table getInstance()
    {
        return new Table();
    }
}
