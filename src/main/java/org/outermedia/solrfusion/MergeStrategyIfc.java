package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.Merge;
import org.outermedia.solrfusion.response.HighlightingMap;
import org.outermedia.solrfusion.response.parser.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Identify response documents which represent the same object and merge them.
 *
 * @author ballmann
 */

public interface MergeStrategyIfc extends Initiable<Merge>
{
    public String getFusionField();

    /**
     * Merge equal documents into one. If the merge field is not unique in one server several merged documents are
     * returned.
     *
     *
     * @param mergeFusionField
     * @param config
     * @param sameDocuments
     * @param allHighlighting
     * @param mergedHighlighting
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public List<Document> mergeDocuments(String mergeFusionField, Configuration config, Collection<Document> sameDocuments,
        HighlightingMap allHighlighting, Map<String, Document> mergedHighlighting)
        throws InvocationTargetException, IllegalAccessException;

}
