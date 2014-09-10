package org.outermedia.solrfusion.response.freemarker;

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
