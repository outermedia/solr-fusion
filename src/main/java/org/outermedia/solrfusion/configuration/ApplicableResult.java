package org.outermedia.solrfusion.configuration;

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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Because patterns are supported in mappings, the specific field name is needed for further processing (instead of
 * the pattern). This class stores the specific field name and the mapping which produced the name.
 *
 * Created by ballmann on 6/20/14.
 */
@Getter
@Setter
@AllArgsConstructor
public class ApplicableResult
{
    private String destinationFieldName;

    private FieldMapping mapping;
}
