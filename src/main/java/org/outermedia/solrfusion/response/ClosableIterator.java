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
