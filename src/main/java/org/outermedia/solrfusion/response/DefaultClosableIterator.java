package org.outermedia.solrfusion.response;

import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.response.parser.Document;

import java.util.List;

/**
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
        setExtraInfo(new SearchServerResponseInfo(totalHits, null, null));
    }

}
