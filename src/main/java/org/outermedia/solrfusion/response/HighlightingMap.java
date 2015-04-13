package org.outermedia.solrfusion.response;

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

import lombok.Getter;
import lombok.Setter;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal representation of highlights.
 *
 * Created by ballmann on 8/6/14.
 */
public class HighlightingMap
{
    @Setter
    protected List<String> searchServerNames;
    @Setter
    private boolean docMergingEnabled;
    @Getter
    protected Map<String, Document> map;
    protected String fusionIdField;
    protected IdGeneratorIfc idGen;

    public HighlightingMap(boolean docMergingEnabled, List<String> searchServerNames)
    {
        map = new HashMap<>();
        this.docMergingEnabled = docMergingEnabled;
        this.searchServerNames = searchServerNames;
    }

    public void init(IdGeneratorIfc idGen)
    {
        fusionIdField = idGen.getFusionIdField();
        this.idGen = idGen;
    }

    public void put(Document doc)
    {
        map.put(doc.getFusionDocId(fusionIdField), doc);
    }

    /**
     * Get the highlighting for a given fusion doc id.
     *
     * @param fusionDocId is maybe the merged fusion doc id of a merged document
     * @return null or a Document instance
     */
    public Document get(String fusionDocId)
    {
        // we need the simple fusion doc id to lookup the highlighting
        if(docMergingEnabled && idGen.isMergedDocument(fusionDocId, searchServerNames))
        {
            fusionDocId = idGen.splitMergedId(fusionDocId).get(0);
        }
        return map.get(fusionDocId);
    }
}
