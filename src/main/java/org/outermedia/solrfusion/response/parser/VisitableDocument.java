package org.outermedia.solrfusion.response.parser;

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

import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 04.06.14.
 */
public interface VisitableDocument
{
    /**
     * A visitor can visit all fields of a response document.
     *
     * @param visitor the implementing instance visits every field of a response document.
     * @param env contains values which are needed when the field is mapped
     * @return null or an SolrSingleValuedField instance where visiting stopped
     */
    public SolrField accept(FieldVisitor visitor, ScriptEnv env);
}
