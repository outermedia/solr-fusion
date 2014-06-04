package org.outermedia.solrfusion.response.parser;

import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 04.06.14.
 */
public interface VisitableDocument
{
    /**
     * A visitor can visit all fields of a response document.
     *
     * @param visitor the implementing instance visits every field of a response document.
     * @param env contains values which are needed when the field is mapped
     * @return null or an SolrField instance where visiting stopped
     */
    public SolrField accept(FieldVisitor visitor, ScriptEnv env);
}
