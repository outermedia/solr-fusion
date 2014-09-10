package org.outermedia.solrfusion.adapter;

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

import org.outermedia.solrfusion.response.ClosableIterator;

import java.util.Iterator;
import java.util.List;

/**
 * A special iterator which allows to close (stop) the iteration. Also an additional context can be saved in the
 * iterator. The iterated content is based on a list.
 *
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
