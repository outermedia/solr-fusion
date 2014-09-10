package org.outermedia.solrfusion.response;

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
