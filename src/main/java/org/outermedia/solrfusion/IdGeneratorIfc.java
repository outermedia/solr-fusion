package org.outermedia.solrfusion;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    public String getSearchServerDocIdFromFusionId(String fusionDocId, List<String> searchServerNames);

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
     * @param strings
     * @return true if the document was merged, otherwise false
     */
    public boolean isMergedDocument(String fusionDocId, List<String> strings);
}
