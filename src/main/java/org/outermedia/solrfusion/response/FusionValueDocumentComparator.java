package org.outermedia.solrfusion.response;

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

import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.Comparator;

/**
 * A special Solr document comparator used when documents are sorted manually.
 * <p/>
 * Created by ballmann on 7/10/14.
 */
public class FusionValueDocumentComparator implements Comparator<Document>
{
    private String fusionSortField;
    private boolean sortAsc;

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
