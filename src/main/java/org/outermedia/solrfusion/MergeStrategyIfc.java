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
