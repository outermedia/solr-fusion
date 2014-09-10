package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;
import org.outermedia.solrfusion.configuration.Initiable;

import java.util.List;

/**
 * Unified/merged documents need a (compound) id, which is created by implementations of this interface.
 */
public interface IdGeneratorIfc extends Initiable<IdGeneratorFactory>
{
    /**
     * The computation of the returned SolrFusion document id has to ensure that the document is directly retrievable
     * from the search server.
     *
     * @param serverName        is the server which returns a document (hit)
     * @param searchServerDocId is the id of document returned by the server named serverName
     * @return a non null String object
     */
    public String computeId(String serverName, String searchServerDocId);

    /**
     * Get the fusion id field name.
     *
     * @return the field name
     */
    public String getFusionIdField();

    /**
     * Retrieve the Solr search server name (used in SolrFusion's schema) from a SolrFusion document id.
     *
     * @param fusionDocId
     * @return
     */
    public String getSearchServerIdFromFusionId(String fusionDocId);

    /**
     * Retrieve the Solr document id from a SolrFusion document id.
     *
     * @param fusionDocId
     * @return
     */
    public String getSearchServerDocIdFromFusionId(String fusionDocId);

    /**
     * Merge 'otherId' into 'thisId'. {@link #splitMergedId(String)} is able to split the result again.
     *
     * @param thisId  not null
     * @param otherId not null
     * @return null or the merged id
     */
    public String mergeIds(String thisId, String otherId);

    /**
     * Split id previously merged by {@link #mergeIds(String, String)}
     *
     * @param mergedIds
     * @return a perhaps empty list
     */
    public List<String> splitMergedId(String mergedIds);

    /**
     * Checks whether the specified SolrFusion document id belongs to a document which was merged from several Solr
     * servers.
     *
     * @param fusionDocId
     * @return true if the document was merged, otherwise false
     */
    public boolean isMergedDocument(String fusionDocId);
}
