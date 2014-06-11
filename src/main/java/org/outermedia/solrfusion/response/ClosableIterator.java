package org.outermedia.solrfusion.response;

import java.util.Iterator;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ClosableIterator<T> extends Iterator<T>
{
    // forget waiting results
    public void close();

    public int size();
}
