package org.outermedia.solrfusion.response;

import java.util.Iterator;

/**
 * A special iterator which allows to close (stop) the iteration. Also an additional context can be saved in the
 * iterator.
 *
 * Created by ballmann on 04.06.14.
 */
public interface ClosableIterator<T, S> extends Iterator<T>
{
    /**
     * Forget waiting results.
     */
    public void close();

    /**
     * Get the total number of entries.
     * @return the number
     */
    public int size();

    /**
     * Get the context object.
     * @return perhaps null
     */
    public S getExtraInfo();

    /**
     * Set the context object.
     * @param info
     */
    public void setExtraInfo(S info);
}
