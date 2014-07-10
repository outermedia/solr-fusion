package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.Comparator;

/**
* Created by ballmann on 7/10/14.
*/
public class FusionValueDocumentComparator implements Comparator<Document>
{
    private final String fusionSortField;
    private final boolean sortAsc;

    public FusionValueDocumentComparator(String fusionSortField, boolean sortAsc)
    {
        this.fusionSortField = fusionSortField;
        this.sortAsc = sortAsc;
    }

    @Override public int compare(Document d1, Document d2)
    {
        Term t1 = d1.getFieldTermByFusionName(fusionSortField);
        Term t2 = d2.getFieldTermByFusionName(fusionSortField);
        if (t1 == null && t2 == null)
        {
            return 0;
        }
        int r;
        if (t1 == null)
        {
            r = t2.compareFusionValue(t1);
            if (sortAsc)
            {
                r = -r;
            }
        }
        else
        {
            r = t1.compareFusionValue(t2);
            if (!sortAsc)
            {
                r = -r;
            }
        }
        return r;
    }
}
