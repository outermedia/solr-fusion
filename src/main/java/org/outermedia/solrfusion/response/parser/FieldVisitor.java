package org.outermedia.solrfusion.response.parser;

import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 04.06.14.
 */
public interface FieldVisitor
{
    /**
     * Visit a solr field in a response document.
     *
     * @param sf one solr field
     * @param env contains values which are needed when the field is mapped
     * @return false to stop visiting otherwise true
     */
    public boolean visitField(SolrField sf, ScriptEnv env);
}
