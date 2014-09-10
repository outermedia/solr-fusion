package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ControllerFactory;
import org.outermedia.solrfusion.configuration.Initiable;

import java.lang.reflect.InvocationTargetException;

/**
 * The whole processing - handling a SolrFusion query until sending back a Solr response - is controlled by
 * implementations of this interface.
 * <p/>
 * Created by ballmann on 6/17/14.
 */
public interface FusionControllerIfc extends Initiable<ControllerFactory>
{
    /**
     *  Process one SolrFusion request.
     *
     * @param configuration     the SolrFusion schema
     * @param fusionRequest     the current SolrFusion request
     * @param fusionResponse    the current SolrFusion response
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    void process(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse)
        throws InvocationTargetException, IllegalAccessException;
}
