package org.outermedia.solrfusion.response;

import java.util.Iterator;
import java.util.List;

/**
* Created by ballmann on 6/6/14.
*/
public class TestClosableStringIterator implements ClosableIterator<String>
{
    boolean calledClose = false;
    Iterator<String> it;

    TestClosableStringIterator(List<String> elements)
    {
        this.it = elements.iterator();
    }

    @Override
    public void close()
    {
        it = null;
        calledClose = true;
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
