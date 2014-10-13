package org.outermedia.solrfusion.mapper;

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

import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Collection;

/**
 * Apply the configured mappings to a Solr response.
 *
 * Created by ballmann on 6/11/14.
 */
public interface ResponseMapperIfc extends FieldVisitor, Initiable<ResponseMapperFactory>
{
    public String DOC_FIELD_NAME_SCORE = "score";
    public String FUSION_FIELD_NAME_SCORE = "score";

    /**
     * Map one Solr document, so that the document becomes compliant with the SolrFusion schema.
     * @param config    the SolrFusion schema
     * @param serverConfig  the current destination Solr server configuration
     * @param doc   the Solr document to map
     * @param env   the context information
     * @param searchServerFieldNamesToMap if null map all fields otherwise only the fields contained in this list
     * @param target    for which response part this response builder is called
     * @return the number of mapped fields (added ones included)
     */
    public int mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env,

        Collection<String> searchServerFieldNamesToMap, ResponseTarget target, boolean applyAddRules);

    /**
     * Per default an exception is thrown if no mapping for a field is found. A call of this methods switches this
     * policy to log a warn message only.
     */
    public void ignoreMissingMappings();
}
