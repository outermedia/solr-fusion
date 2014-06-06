package org.outermedia.solrfusion;

import org.outermedia.solrfusion.response.ClosableIterator;

import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class RoundRobinClosableIterator<T> implements ClosableIterator<T>
{
    private final List<ClosableIterator<T>> iterators;
    private int at;

    public RoundRobinClosableIterator(List<ClosableIterator<T>> iterators)
    {
        this.iterators = iterators;
        at = 0;
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
