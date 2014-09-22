package org.outermedia.solrfusion.response.freemarker;

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

import lombok.Getter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.response.parser.DocCount;

import java.util.List;
import java.util.Map;

/**
 * Dataholder used to provide facet information when a Solr response is rendered.
 *
 * Created by ballmann on 8/11/14.
 */
public class FreemarkerFacets
{
    @Getter
    private final Map<String, List<DocCount>> facets;

    @Getter
    private boolean hasFacets;

    public FreemarkerFacets(Configuration configuration, Map<String, List<DocCount>> facets)
    {
        this.facets = facets;
        hasFacets = facets != null && !facets.isEmpty();
    }
}
