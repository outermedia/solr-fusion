package org.outermedia.solrfusion.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
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
