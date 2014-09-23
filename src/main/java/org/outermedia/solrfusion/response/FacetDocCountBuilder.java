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

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.response.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-convert the internally used Solr doc representation of facets into a format suitable to render in a response.
 * <p/>
 * Created by ballmann on 8/11/14.
 */
@Slf4j
public class FacetDocCountBuilder implements FieldVisitor
{
    private String fusionIdField;
    private IdGeneratorIfc idGenerator;
    private Document doc;
    private Map<String, Map<String, Integer>> fusionFacetFields;

    /**
     * Fill a specified empty map (fusionFacetFields) from the specified Solr document (doc) which contains the facets.
     *
     * @param fusionIdField
     * @param idGenerator
     * @param doc               contains the facets as fields (the doc counts are set too)
     * @param fusionFacetFields this parameter is filled from the processed facets. The key maps a field to a map of
     *                          words and their doc counts.
     */
    public FacetDocCountBuilder(String fusionIdField, IdGeneratorIfc idGenerator, Document doc,
        Map<String, Map<String, Integer>> fusionFacetFields)
    {
        this.fusionIdField = fusionIdField;
        this.idGenerator = idGenerator;
        this.doc = doc;
        this.fusionFacetFields = fusionFacetFields;
    }

    @Override public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
    {
        handleSolrField(sf);
        return true;
    }

    protected void handleSolrField(SolrField sf)
    {
        if (!sf.isFusionField(fusionIdField) && !sf.isFusionField("score"))
        {
            List<Integer> docCount = sf.getFusionFacetCount();
            // because all mappings are applied, perhaps some fields were added which are not facet fields
            // these fields have no doc count set and are ignored
            if (docCount != null && sf.isProcessed() && !sf.isRemoved())
            {
                List<String> values = sf.getAllFusionFieldValue();
                if (docCount.size() != values.size())
                {
                    log.error(
                        "Mapping didn't fix facet doc count for field: '{}' of server {}. Facet's doc count is ignored.",
                        sf.getTerm().getSearchServerFieldName(),
                        idGenerator.getSearchServerIdFromFusionId(doc.getFusionDocId(fusionIdField)));
                }
                else
                {
                    String fusionFieldName = sf.getFusionFieldName();
                    Map<String, Integer> fusionDocCount = fusionFacetFields.get(fusionFieldName);
                    if (fusionDocCount == null)
                    {
                        fusionDocCount = new HashMap<>();
                        fusionFacetFields.put(fusionFieldName, fusionDocCount);
                    }
                    for (int i = 0; i < docCount.size(); i++)
                    {
                        String word = values.get(i);
                        Integer dcObj = fusionDocCount.get(word);
                        int dc = 0;
                        if (dcObj != null)
                        {
                            dc = dcObj;
                        }
                        dc += docCount.get(i);
                        if (log.isDebugEnabled() && dc != docCount.get(i))
                        {
                            log.trace("MERGED FACET DOC COUNTS OF {}: {} to {}+{}={}", sf.getFusionFieldName(), word,
                                dcObj, docCount.get(i), dc);
                        }
                        if (dc > 0)
                        {
                            fusionDocCount.put(word, dc);
                        }
                    }
                }
            }
        }
    }

    @Override public boolean visitField(SolrMultiValuedField msf, ScriptEnv env)
    {
        handleSolrField(msf);
        return true;
    }
}
