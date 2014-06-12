package org.outermedia.solrfusion.response;

import java.util.Iterator;

/**
 * Created by ballmann on 04.06.14.
 */
public interface ClosableIterator<T, S> extends Iterator<T>
{
    // forget waiting results
    public void close();

    public int size();

    public S getExtraInfo();

    public void setExtraInfo(S info);
}
