package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * A util class to return a list of targets and the SolrFusion and Solr field name of a mapping.
 *
 * Created by ballmann on 8/13/14.
 */
@Getter
@Setter
public class TargetsOfMapping extends ArrayList<Target>
{
    private String mappingsSearchServerFieldName;
    private String mappingsFusionFieldName;
}
