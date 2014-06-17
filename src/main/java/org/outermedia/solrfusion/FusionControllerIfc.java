package org.outermedia.solrfusion;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ControllerFactory;
import org.outermedia.solrfusion.configuration.Initiable;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ballmann on 6/17/14.
 */
public interface FusionControllerIfc extends Initiable<ControllerFactory>
{
    void process(Configuration configuration, FusionRequest fusionRequest, FusionResponse fusionResponse)
            throws InvocationTargetException, IllegalAccessException;
}
