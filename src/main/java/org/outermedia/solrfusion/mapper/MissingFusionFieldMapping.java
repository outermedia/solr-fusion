package org.outermedia.solrfusion.mapper;

/**
 * This exception is used when a SolrFusion field is not mapped to a Solr field.
 *
 * Created by ballmann on 04.06.14.
 */
public class MissingFusionFieldMapping extends RuntimeException
{
    public MissingFusionFieldMapping(String message)
    {
        super(message);
    }
}
