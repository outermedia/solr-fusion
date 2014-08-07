package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ballmann on 8/6/14.
 */
public class HighlightingMap
{
    private Map<String, Document> map;
    private String fusionIdField;
    private IdGeneratorIfc idGen;

    public HighlightingMap()
    {
        map = new HashMap<>();
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
        if(idGen.isMergedDocument(fusionDocId))
        {
            fusionDocId = idGen.splitMergedId(fusionDocId).get(0);
        }
        return map.get(fusionDocId);
    }
}
