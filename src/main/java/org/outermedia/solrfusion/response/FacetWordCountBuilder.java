package org.outermedia.solrfusion.response;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.response.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by ballmann on 8/11/14.
*/
@Slf4j
public class FacetWordCountBuilder implements FieldVisitor
{
    private final String fusionIdField;
    private final IdGeneratorIfc idGenerator;
    private final Document doc;
    private final Map<String, Map<String, Integer>> fusionFacetFields;

    public FacetWordCountBuilder(String fusionIdField, IdGeneratorIfc idGenerator, Document doc,
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
            List<Integer> wordCount = sf.getFusionFacetCount();
            // because all mappings are applied, perhaps some fields were added which are not facet fields
            // these fields have no word count set and are ignored
            if (wordCount != null && sf.isProcessed() && !sf.isRemoved())
            {
                List<String> values = sf.getAllFusionFieldValue();
                if (wordCount.size() != values.size())
                {
                    log.error("Mapping didn't fix facet word count for field: '{}' of server {}. Facet's word count is ignored.",
                        sf.getTerm().getSearchServerFieldName(),
                        idGenerator.getSearchServerIdFromFusionId(doc.getFusionDocId(fusionIdField)));
                }
                else
                {
                    String fusionFieldName = sf.getFusionFieldName();
                    Map<String, Integer> fusionWordCount = fusionFacetFields.get(fusionFieldName);
                    if(fusionWordCount == null)
                    {
                        fusionWordCount = new HashMap<>();
                        fusionFacetFields.put(fusionFieldName, fusionWordCount);
                    }
                    for(int i=0; i<wordCount.size(); i++)
                    {
                        String word = values.get(i);
                        Integer wcObj = fusionWordCount.get(word);
                        int wc = 0;
                        if(wcObj != null) wc = wcObj;
                        wc += wordCount.get(i);
                        fusionWordCount.put(word, wc);
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
