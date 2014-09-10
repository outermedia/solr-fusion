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

import java.util.List;

/**
 * A generic simple ClosableIterator which uses the round-robin approach to return one single iterator from the
 * specified multiple iterators.
 * <p/>
 * Created by ballmann on 04.06.14.
 */
public class RoundRobinClosableIterator<T, S> implements ClosableIterator<T, S>
{
    private final List<? extends ClosableIterator<T, S>> iterators;
    private int at;
    private int size;
    private S info;

    public RoundRobinClosableIterator(List<? extends ClosableIterator<T, S>> iterators, S info)
    {
        this.iterators = iterators;
        this.info = info;
        at = 0;
        size = 0;
        for (ClosableIterator<T, S> it : iterators)
        {
            size += it.size();
        }
    }

    @Override
    public void close()
    {
        for (ClosableIterator<T, S> it : iterators)
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

    public ClosableIterator<T, S> getCurrentIterator()
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
