package org.outermedia.solrfusion.types;

import lombok.ToString;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Environment which is passed to script types. E.g. the current fusion field is contained.
 * <p/>
 * Created by ballmann on 03.06.14.
 * <p/>
 * The following env entries are available in a ScriptType: <ul> <li>{@value #ENV_IN_FUSION_FIELD} - a String</li>
 * <li>{@value #ENV_IN_FUSION_VALUE} - a List of String</li> <li>{@value #ENV_IN_SEARCH_SERVER_FIELD} - a String</li>
 * <li>{@value #ENV_IN_SEARCH_SERVER_VALUE} - a List of String</li> <li>{@value #ENV_IN_FUSION_FIELD_DECLARATION} - a
 * FusionField instance</li> <li>{@value #ENV_IN_FUSION_SCHEMA} - a Configuration instance</li> <li>{@value
 * #ENV_IN_VALUES} - a List of String</li> <li>{@value #ENV_IN_CONVERSION} - a ConversionDirection</li><li>{@value
 * #ENV_IN_LOCALE} - a Locale</li> <li>{@value #ENV_IN_DOCUMENT} - a Document</li><li>{@value #ENV_IN_TERM_QUERY_PART} -
 * a TermQuery</li> <li>{@value #ENV_IN_SEARCH_SERVER_CONFIG} - a SearchServerConfig</li><li>{@value #ENV_IN_WORD_COUNT}
 * - a list of int</li><li>{@value #ENV_IN_DOC_TERM} - a Term</li></ul> A ScriptType has to set the following entries:
 * <ul><li>{@value #ENV_OUT_NEW_VALUES} - null or a list of processed values</li> <li>{@value #ENV_OUT_NEW_WORD_COUNTS}
 * - a list of new word counts (default value is copied from {@value #ENV_IN_WORD_COUNT})</li></ul>
 */
@ToString
public class ScriptEnv
{
    private Map<String, Object> bindings;
    private ScriptEnv parentBindings;

    // variables passed into a script
    public final static String ENV_IN_FUSION_FIELD = "fusionField"; // a String
    public final static String ENV_IN_FUSION_VALUE = "fusionValue"; // a String
    public final static String ENV_IN_SEARCH_SERVER_FIELD = "searchServerField"; // a String
    public final static String ENV_IN_SEARCH_SERVER_VALUE = "searchServerValue"; // a String
    public final static String ENV_IN_FUSION_FIELD_DECLARATION = "fusionFieldDeclaration"; // a FusionField
    public final static String ENV_IN_FUSION_SCHEMA = "fusionSchema"; // a Configuration
    public final static String ENV_IN_VALUES = "values"; // a List of String
    public final static String ENV_IN_CONVERSION = "conversion"; // a ConversionDirection
    public final static String ENV_IN_LOCALE = "locale"; // a Locale
    public final static String ENV_IN_DOCUMENT = "responseDocument"; // a Document
    public final static String ENV_IN_TERM_QUERY_PART = "termQueryPart"; // a TermQuery
    public final static String ENV_IN_SEARCH_SERVER_CONFIG = "searchServerConfig"; // a SearchServerConfig
    public final static String ENV_IN_WORD_COUNT = "facetWordCount"; // a List<Integer> for facets only
    public final static String ENV_IN_DOC_TERM = "docFieldTerm"; // a Term
    public final static String ENV_IN_FUSION_REQUEST = "fusionRequest"; // a FusionRequest
    public final static String ENV_IN_MAP_FACET = "mapFacetValue"; // a Boolean
    public final static String ENV_IN_MAP_HIGHLIGHT = "mapHighlightValue"; // a Boolean

    // variables set by a script
    public final static String ENV_OUT_NEW_VALUES = "returnValues";
    public final static String ENV_OUT_NEW_WORD_COUNTS = "returnWordCounts";

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

    public boolean getBoolBinding(String name)
    {
        Boolean boolResult = (Boolean) getBinding(name);
        if (boolResult == null)
        {
            return false;
        }
        return boolResult;
    }

    public void setConfiguration(Configuration cfg)
    {
        setBinding(ENV_IN_FUSION_SCHEMA, cfg);
    }

    public Configuration getConfiguration()
    {
        return (Configuration) getBinding(ENV_IN_FUSION_SCHEMA);
    }

    public void setLocale(Locale l)
    {
        setBinding(ENV_IN_LOCALE, l);
    }

    public Locale getLocale()
    {
        return (Locale) getBinding(ENV_IN_LOCALE);
    }

    public void setDocument(Document doc)
    {
        setBinding(ENV_IN_DOCUMENT, doc);
    }

    public Document getDocument()
    {
        return (Document) getBinding(ENV_IN_DOCUMENT);
    }

    public SearchServerConfig getSearchServerConfig()
    {
        return (SearchServerConfig) getBinding(ENV_IN_SEARCH_SERVER_CONFIG);
    }

    public void setSearchServerConfig(SearchServerConfig config)
    {
        setBinding(ENV_IN_SEARCH_SERVER_CONFIG, config);
    }

}
