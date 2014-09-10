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

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Merge;
import org.outermedia.solrfusion.configuration.MergeTarget;
import org.outermedia.solrfusion.response.HighlightingMap;
import org.outermedia.solrfusion.response.parser.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Identify response documents which represent the same object and merge them and their highlights.
 *
 * @author ballmann
 */

@ToString
@Slf4j
public class DefaultMergeStrategy implements MergeStrategyIfc
{
    @Getter
    private String fusionField;
    private List<String> serverPrio;

    /**
     * Factory creates instances only.
     */
    protected DefaultMergeStrategy()
    {
    }

    public static class Factory
    {
        public static DefaultMergeStrategy getInstance()
        {
            return new DefaultMergeStrategy();
        }
    }

    @Override
    public void init(Merge config)
    {
        this.fusionField = config.getFusionName();
        serverPrio = new ArrayList<>();
        List<MergeTarget> sortedList = new ArrayList<>();
        sortedList.addAll(config.getTargets());
        Collections.sort(sortedList, new Comparator<MergeTarget>()
        {
            @Override public int compare(MergeTarget o1, MergeTarget o2)
            {
                if (o1.getPrio() == o2.getPrio())
                {
                    return 0;
                }
                if (o1.getPrio() < o2.getPrio())
                {
                    return -1;
                }
                return 1;
            }
        });
        serverPrio = new ArrayList<>(sortedList.size());
        for (MergeTarget mt : sortedList)
        {
            serverPrio.add(mt.getTargetName());
        }
    }

    /**
     * Merge several documents of the same book/news paper into one instance.
     *
     * @param mergeFusionField
     * @param config
     * @param sameDocuments      a non null list of documents which describe the same book/news paper
     * @param allHighlighting    perhaps empty
     * @param mergedHighlighting perhaps null
     * @return null or a list of document instance (not empty)
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public List<Document> mergeDocuments(String mergeFusionField, Configuration config,
        Collection<Document> sameDocuments, HighlightingMap allHighlighting, Map<String, Document> mergedHighlighting)
        throws InvocationTargetException, IllegalAccessException
    {
        String fusionIdField = config.getFusionIdFieldName();
        List<Document> mergedDocs = null;
        IdGeneratorIfc idHandler = config.getIdGenerator();
        // find base document to use, respecting the given priority
        int at = 0;
        while (at < serverPrio.size())
        {
            mergedDocs = findDocumentsOfServer(serverPrio.get(at), sameDocuments, idHandler);
            if (mergedDocs == null)
            {
                at++;
            }
            else
            {
                break;
            }
        }
        if (mergedDocs != null)
        {
            at++;
            int currentAt = at;
            for (Document mergedDoc : mergedDocs)
            {
                at = currentAt;
                Document mergedHl = allHighlighting.get(mergedDoc.getFusionDocId(fusionIdField));
                while (at < serverPrio.size())
                {
                    List<Document> docsToMerge = findDocumentsOfServer(serverPrio.get(at), sameDocuments, idHandler);
                    if (docsToMerge != null)
                    {
                        for (Document toMerge : docsToMerge)
                        {
                            mergedDoc.addUnsetFusionFieldsOf(toMerge, idHandler);
                            List<String> mergeFieldValues = mergedDoc.getFusionValuesOf(mergeFusionField);
                            List<String> mergeFieldValuesToMerge = toMerge.getFusionValuesOf(mergeFusionField);
                            mergedDoc.replaceFusionValuesOf(mergeFusionField,
                                append(mergeFieldValues, mergeFieldValuesToMerge));
                            // mergedHl should not be null, because otherwise doc wouldn't be found
                            // but we check it nevertheless
                            Document hlToMerge = allHighlighting.get(toMerge.getFusionDocId(fusionIdField));
                            if (hlToMerge != null)
                            {
                                if (mergedHl == null)
                                {
                                    mergedHl = new Document();
                                }
                                mergedHl.addUnsetFusionFieldsOf(hlToMerge, idHandler);
                            }
                        }
                    }
                    at++;
                }

                // merging modified fusion doc id!
                String fusionDocId = mergedDoc.getFusionDocId(fusionIdField);
                if (fusionDocId != null && mergedHl != null && mergedHighlighting != null)
                {
                    // set the doc id in order to ensure that the merged highlighting will be found for the merged document
                    mergedHl.setFusionDocId(fusionIdField, fusionDocId);
                    mergedHighlighting.put(fusionDocId, mergedHl);
                }
            }
        }

        return mergedDocs;
    }

    protected List<String> append(List<String> mergeFieldValues, List<String> mergeFieldValuesToMerge)
    {
        for (String v : mergeFieldValuesToMerge)
        {
            if (!mergeFieldValues.contains(v))
            {
                mergeFieldValues.add(v);
            }
        }
        return mergeFieldValues;
    }

    protected List<Document> findDocumentsOfServer(String searchServerName, Collection<Document> documents,
        IdGeneratorIfc idHandler)
    {
        List<Document> result = null;
        String fusionIdField = idHandler.getFusionIdField();
        for (Document doc : documents)
        {
            String fusionDocId = doc.getFusionDocId(fusionIdField);
            if (searchServerName.equals(idHandler.getSearchServerIdFromFusionId(fusionDocId)))
            {
                if (result == null)
                {
                    result = new ArrayList<>();
                }
                result.add(doc);
            }
        }
        return result;
    }
}
