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

import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.List;

/**
 * A simple ClosableIterator which uses the round-robin approach to return one single iterator from the specified
 * multiple iterators. This class is a special instance returning Solr documents and storing SearchServerResponseInfo
 * objects as extra info.
 *
 * Created by ballmann on 6/12/14.
 */
public class DefaultClosableIterator extends RoundRobinClosableIterator<Document,SearchServerResponseInfo>
{
    public DefaultClosableIterator(List<? extends ClosableIterator<Document, SearchServerResponseInfo>> closableIterators)
    {
        super(closableIterators, null);
        int totalHits = 0;
        for(ClosableIterator<Document,SearchServerResponseInfo> ci : closableIterators){
            totalHits += ci.getExtraInfo().getTotalNumberOfHits();
        }
        setExtraInfo(new SearchServerResponseInfo(totalHits, null, null, null));
    }

}
