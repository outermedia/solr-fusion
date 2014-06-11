package org.outermedia.solrfusion.adapter;

import org.outermedia.solrfusion.response.ClosableIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ballmann on 6/11/14.
 */
public class ClosableListIterator<T> implements ClosableIterator<T>
{
    private Iterator<T> listIt;
    private int size;

    public ClosableListIterator(List<T> list)
    {
        this.listIt = list.iterator();
        size = list.size();
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
    public boolean hasNext()
    {
        return listIt.hasNext();
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
