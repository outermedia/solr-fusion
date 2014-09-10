package org.outermedia.solrfusion.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Because patterns are supported in mappings, the specific field name is needed for further processing (instead of
 * the pattern). This class stores the specific field name and the mapping which produced the name.
 *
 * Created by ballmann on 6/20/14.
 */
@Getter
@Setter
@AllArgsConstructor
public class ApplicableResult
{
    private String destinationFieldName;

    private FieldMapping mapping;
}
