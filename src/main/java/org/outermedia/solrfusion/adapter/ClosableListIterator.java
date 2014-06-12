package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.response.ClosableIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ballmann on 6/11/14.
 */
public class ClosableListIterator<T, S> implements ClosableIterator<T, S>
{
    private Iterator<T> listIt;
    private int size;
    private S info;

    public ClosableListIterator(List<T> list, S info)
    {
        this.listIt = null;
        size = 0;
        if (list != null)
        {
            listIt = list.iterator();
            size = list.size();
        }
        this.info = info;
    }

    @Override
    public void close()
    {
        listIt = null;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public S getExtraInfo()
    {
        return info;
    }

    @Override
    public void setExtraInfo(S info)
    {
        this.info = info;
    }

    @Override
    public boolean hasNext()
    {
        return listIt != null && listIt.hasNext();
    }

    @Override
    public T next()
    {
        return listIt.next();
    }

    /**
     * This method always throws a runtime exception ("not supported").
     */
    @Override
    public void remove()
    {
        throw new RuntimeException("Not supported");
    }
}
