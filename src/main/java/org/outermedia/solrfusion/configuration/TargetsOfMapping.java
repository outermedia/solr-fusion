package org.outermedia.solrfusion.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by ballmann on 8/13/14.
 */
@Getter
@Setter
public class TargetsOfMapping extends ArrayList<Target>
{
    private String mappingsSearchServerFieldName;
    private String mappingsFusionFieldName;
}
