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
 * Merge Solr documents from different Solr servers which were identified to be the same.
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
     * @param mergeFusionField   the SolrFusion field which is used for merging
     * @param config             the SolrFusion schema
     * @param sameDocuments      the documents to merge
     * @param allHighlighting       the corresponding highlights of the "sameDocuments", perhaps empty
     * @param mergedHighlighting    perhaps null, but if not null, implementations have to add the merged highlights
     * @return                      null or a list of document instance (not empty)
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public List<Document> mergeDocuments(String mergeFusionField, Configuration config,
        Collection<Document> sameDocuments, HighlightingMap allHighlighting, Map<String, Document> mergedHighlighting)
        throws InvocationTargetException, IllegalAccessException;

}
