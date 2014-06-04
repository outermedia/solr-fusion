package org.outermedia.solrfusion.mapper;

/**
 * Created by ballmann on 04.06.14.
 */
public class MissingSearchServerFieldMapping extends RuntimeException
{
    public MissingSearchServerFieldMapping(String message)
    {
        super(message);
    }
}
