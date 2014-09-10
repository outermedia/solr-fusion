package org.outermedia.solrfusion.mapper;

/**
 * This exception is thrown when no mapping of Solr field to a SolrFusion field exists.
 *
 * Created by ballmann on 04.06.14.
 */
public class MissingSearchServerFieldMapping extends RuntimeException
{
    public MissingSearchServerFieldMapping(String message)
    {
        super(message);
    }
}
