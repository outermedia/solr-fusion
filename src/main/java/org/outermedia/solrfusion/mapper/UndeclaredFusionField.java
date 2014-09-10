package org.outermedia.solrfusion.mapper;

/**
 * This exception is thrown when an undeclared SolrFusion field is used.
 *
 * Created by ballmann on 04.06.14.
 */
public class UndeclaredFusionField extends RuntimeException
{
    public UndeclaredFusionField(String message)
    {
        super(message);
    }
}
