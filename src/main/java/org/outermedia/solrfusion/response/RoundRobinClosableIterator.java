package org.outermedia.solrfusion.response;

import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class RoundRobinClosableIterator<T> implements ClosableIterator<T>
{
    private final List<? extends ClosableIterator<T>> iterators;
    private int at;
    private int size;

    public RoundRobinClosableIterator(List<? extends ClosableIterator<T>> iterators)
    {
        this.iterators = iterators;
        at = 0;
        size = 0;
        for (ClosableIterator<T> it : iterators)
        {
            size += it.size();
        }
    }

    @Override
    public void close()
    {
        for (ClosableIterator<T> it : iterators)
        {
            it.close();
        }
        iterators.clear();
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public boolean hasNext()
    {
        boolean result = false;
        if (iterators.size() > 0)
        {
            do
            {
                if (at >= iterators.size())
                {
                    at = 0;
                }
                result = iterators.get(at).hasNext();
                if (!result)
                {
                    iterators.remove(at).close();
                }
            }
            while (!result && iterators.size() > 0);
        }
        return result;
    }

    public ClosableIterator<T> getCurrentIterator()
    {
        return iterators.get(at);
    }

    @Override
    public T next()
    {
        T d = iterators.get(at).next();
        at = (at + 1) % iterators.size();
        return d;
    }

    @Override
    public void remove()
    {
        throw new RuntimeException("Not implemented");
    }
}
