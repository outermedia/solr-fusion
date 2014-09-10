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

import java.util.Iterator;
import java.util.List;

/**
 * Created by ballmann on 6/6/14.
 */
public class TestClosableStringIterator implements ClosableIterator<String, Void>
{
    boolean calledClose = false;
    Iterator<String> it;
    int size;


    TestClosableStringIterator(List<String> elements)
    {
        this.it = elements.iterator();
        size = elements.size();
    }

    @Override
    public void close()
    {
        it = null;
        calledClose = true;
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public Void getExtraInfo()
    {
        return null;
    }

    @Override
    public void setExtraInfo(Void info)
    {
        // NOP
    }

    @Override
    public boolean hasNext()
    {
        return it.hasNext();
    }

    @Override
    public String next()
    {
        return it.next();
    }

    @Override
    public void remove()
    {
        it.remove();
    }
}
