package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Configuration;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

/**
 * Environment which is passed to script types. E.g. the current fusion field is contained.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
@ToString
public class ScriptEnv
{
    private Map<String, Object> bindings;
    private ScriptEnv parentBindings;

    public final static String ENV_FUSION_FIELD = "fusionField"; // a String
    public final static String ENV_FUSION_VALUE = "fusionValue"; // a String
    public final static String ENV_SEARCH_SERVER_FIELD = "searchServerField"; // a String
    public final static String ENV_SEARCH_SERVER_VALUE = "searchServerValue"; // a String
    public final static String ENV_FUSION_FIELD_DECLARATION = "fusionFieldDeclaration"; // a FusionField
    public final static String ENV_FUSION_SCHEMA = "fusionSchema"; // a Configuration

    public ScriptEnv()
    {
        bindings = new HashMap<>();
    }

    public ScriptEnv(ScriptEnv parentEnv)
    {
        this();
        parentBindings = parentEnv;
    }

    public void flatten(Bindings scriptBindings)
    {
        if (parentBindings != null)
        {
            parentBindings.flatten(scriptBindings);
        }
        scriptBindings.putAll(bindings);
    }

    public void setBinding(String name, Object value)
    {
        bindings.put(name, value);
    }

    public Object getBinding(String name)
    {
        Object result = bindings.get(name);
        if (result == null && parentBindings != null)
        {
            result = parentBindings.getBinding(name);
        }
        return result;
    }

    public String getStringBinding(String name)
    {
        Object result = getBinding(name);
        return (String) result;
    }

    public void setConfiguration(Configuration cfg)
    {
        setBinding(ENV_FUSION_SCHEMA, cfg);
    }

    public Configuration getConfiguration()
    {
        return (Configuration) getBinding(ENV_FUSION_SCHEMA);
    }
}
