package org.outermedia.solrfusion.query;

import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Visitor pattern.
 *
 * Created by ballmann on 03.06.14.
 */
public interface VisitableQuery
{
    public void accept(QueryVisitor visitor, ScriptEnv env);
}
