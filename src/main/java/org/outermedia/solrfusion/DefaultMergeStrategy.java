package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.ToString;
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
     * @param config
     * @param sameDocuments      a non null list of documents which describe the same book/news paper
     * @param allHighlighting    perhaps empty
     * @param mergedHighlighting perhaps null
     * @return a document instance
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Document mergeDocuments(Configuration config, Collection<Document> sameDocuments,
        HighlightingMap allHighlighting, Map<String, Document> mergedHighlighting)
        throws InvocationTargetException, IllegalAccessException
    {
        String fusionIdField = config.getFusionIdFieldName();
        Document mergedDoc = null;
        Document mergedHl = null;
        if (sameDocuments.size() == 1)
        {
            // we want a mergedDoc, perhaps the document contains no id!
            mergedDoc = sameDocuments.iterator().next();
            String fusionDocId = mergedDoc.getFusionDocId(fusionIdField);
            if (fusionDocId != null)
            {
                mergedHl = allHighlighting.get(fusionDocId);
            }
        }
        else
        {
            IdGeneratorIfc idHandler = config.getIdGenerator();
            // find base document to use, respecting the given priority
            int at = 0;
            while (at < serverPrio.size())
            {
                mergedDoc = findDocumentOfServer(serverPrio.get(at), sameDocuments, idHandler);
                if (mergedDoc == null)
                {
                    at++;
                }
                else
                {
                    break;
                }
            }
            if (mergedDoc != null)
            {
                mergedHl = allHighlighting.get(mergedDoc.getFusionDocId(fusionIdField));
                at++;
                while (at < serverPrio.size())
                {
                    Document toMerge = findDocumentOfServer(serverPrio.get(at), sameDocuments, idHandler);
                    if (toMerge != null)
                    {
                        mergedDoc.addUnsetFusionFieldsOf(toMerge, idHandler);
                        // mergedHl should not be null, because otherwise doc wouldn't be found
                        // but we check it nevertheless
                        Document hlToMerge = allHighlighting.get(toMerge.getFusionDocId(fusionIdField));
                        if (mergedHl == null)
                        {
                            mergedHl = hlToMerge;
                        }
                        else
                        {
                            mergedHl.addUnsetFusionFieldsOf(hlToMerge, idHandler);
                        }
                    }
                    at++;
                }
            }
        }

        // merging modified fusion doc id!
        String fusionDocId = mergedDoc.getFusionDocId(fusionIdField);
        if (fusionDocId != null && mergedHl != null && mergedHighlighting != null)
        {
            // set the doc id in order to ensure that the merged highlighting will be found for the merged document
            mergedHl.setFusionDocId(fusionIdField, fusionDocId);
            mergedHighlighting.put(fusionDocId, mergedHl);
        }

        return mergedDoc;
    }

    protected Document findDocumentOfServer(String searchServerName, Collection<Document> documents,
        IdGeneratorIfc idHandler)
    {
        Document result = null;
        String fusionIdField = idHandler.getFusionIdField();
        for (Document doc : documents)
        {
            String fusionDocId = doc.getFusionDocId(fusionIdField);
            if (searchServerName.equals(idHandler.getSearchServerIdFromFusionId(fusionDocId)))
            {
                result = doc;
                break;
            }
        }
        return result;
    }
}
