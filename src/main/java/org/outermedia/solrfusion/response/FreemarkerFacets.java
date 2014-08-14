package org.outermedia.solrfusion.response;

import lombok.Getter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.WordCount;

import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 8/11/14.
 */
public class FreemarkerFacets
{
    @Getter
    private final Map<String, List<WordCount>> facets;

    @Getter
    private boolean hasFacets;

    public FreemarkerFacets(Configuration configuration, Map<String, List<WordCount>> facets)
    {
        this.facets = facets;
        hasFacets = facets != null && !facets.isEmpty();
    }
}
