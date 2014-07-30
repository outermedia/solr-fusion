package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.Merge;
import org.outermedia.solrfusion.response.parser.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Identify response documents which represent the same object and merge them.
 * 
 * @author ballmann
 * 
 */

public interface MergeStrategyIfc extends Initiable<Merge>
{
    public String getFusionField();

    public Document mergeDocuments(Configuration config, Collection<Document> sameDocuments)
        throws InvocationTargetException, IllegalAccessException;

}
