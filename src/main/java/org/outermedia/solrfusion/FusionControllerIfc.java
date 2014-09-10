package org.outermedia.solrfusion;

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
