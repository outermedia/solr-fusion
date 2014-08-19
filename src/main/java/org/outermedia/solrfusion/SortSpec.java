package org.outermedia.solrfusion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ballmann on 8/19/14.
 */
@Getter
@Setter
@AllArgsConstructor()
public class SortSpec
{
    private String fusionSortField;
    private String searchServerSortField;
    private boolean sortAsc;
}
