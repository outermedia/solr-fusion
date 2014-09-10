package org.outermedia.solrfusion.response.freemarker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dataholder used to provide highlight information when a Solr response is rendered.
 *
 * Created by ballmann on 8/5/14.
 */
@Slf4j
public class FreemarkerResponseHighlighting
{
    @Getter
    private final List<FreemarkerDocument> highlighting;

    @Getter
    private boolean hasHighlights;

    public FreemarkerResponseHighlighting(Configuration config, Map<String, Document> highlightingMap)
    {
        highlighting = new ArrayList<>();
        if (highlightingMap != null)
        {
            for (Map.Entry<String, Document> entry : highlightingMap.entrySet())
            {
                try
                {
                    // force multi value ignores the field's type in the fusion schema
                    FreemarkerDocument freemarkerDoc = new FreemarkerDocument(true);
                    ScriptEnv env = new ScriptEnv();
                    env.setConfiguration(config);
                    Document d = entry.getValue();
                    freemarkerDoc.setId(d.getFusionDocId(config.getFusionIdFieldName()));
                    // we don't want the single values (id) in the highlighting response
                    d.setSolrSingleValuedFields(null);
                    d.accept(freemarkerDoc, env);
                    highlighting.add(freemarkerDoc);
                }
                catch (Exception e)
                {
                    log.error("Caught exception while preparing response document", e);
                }
            }
        }
        hasHighlights = !highlighting.isEmpty();
    }
}
