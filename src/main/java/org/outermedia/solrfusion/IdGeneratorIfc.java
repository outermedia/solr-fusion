package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;
import org.outermedia.solrfusion.configuration.Initiable;

public interface IdGeneratorIfc extends Initiable<IdGeneratorFactory>
{
    /**
     * The computation of the returned id has to ensure that the document is directly retrievable from the search server.
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
}
