package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;
import org.outermedia.solrfusion.configuration.Initiable;

import java.util.List;

public interface IdGeneratorIfc extends Initiable<IdGeneratorFactory>
{
    /**
     * The computation of the returned id has to ensure that the document is directly retrievable from the search
     * server.
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

    public String getSearchServerIdFromFusionId(String fusionDocId);

    public String getSearchServerDocIdFromFusionId(String fusionDocId);

    /**
     * Merge 'otherId' into 'thisId'. {@link #splitMergedIds(String)} is able to split the result again.
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
     * @return
     */
    public List<String> splitMergedId(String mergedIds);

    public boolean isMergedDocument(String fusionDocId);
}
