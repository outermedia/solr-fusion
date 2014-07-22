package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.Document;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Environment which is passed to script types. E.g. the current fusion field is contained.
 * <p/>
 * Created by ballmann on 03.06.14.
 *
 * The following env entries are available:
 * <ul>
 *     <li>{@value #ENV_FUSION_FIELD} - a String</li>
 *     <li>{@value #ENV_FUSION_VALUE} - a List of String</li>
 *     <li>{@value #ENV_SEARCH_SERVER_FIELD} - a String</li>
 *     <li>{@value #ENV_SEARCH_SERVER_VALUE} - a List of String</li>
 *     <li>{@value #ENV_FUSION_FIELD_DECLARATION} - a FusionField instance</li>
 *     <li>{@value #ENV_FUSION_SCHEMA} - a Configuration instance</li>
 *     <li>{@value #ENV_VALUES} - a List of String</li>
 *     <li>{@value #ENV_CONVERSION} - a ConversionDirection</li>
 * </ul>
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
    public final static String ENV_VALUES = "values"; // a List of String
    public final static String ENV_CONVERSION = "conversion"; // a ConversionDirection
    public final static String ENV_LOCALE = "locale"; // a Locale
    public final static String ENV_DOCUMENT = "responseDocument"; // a Document

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

    public void setLocale(Locale l)
    {
        setBinding(ENV_LOCALE, l);
    }

    public Locale getLocale()
    {
        return (Locale) getBinding(ENV_LOCALE);
    }

    public void setDocument(Document doc)
    {
        setBinding(ENV_DOCUMENT, doc);
    }

    public Document getDocument()
    {
        return (Document) getBinding(ENV_DOCUMENT);
    }
}
