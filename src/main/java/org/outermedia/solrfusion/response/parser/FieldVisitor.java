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
public interface FieldVisitor
{
    /**
     * Visit a solr field in a response document.
     *
     * @param sf one single value solr field
     * @param env contains values which are needed when the field is mapped
     * @return false to stop visiting otherwise true
     */
    public boolean visitField(SolrSingleValuedField sf, ScriptEnv env);

    /**
     * Visit a solr field in a response document.
     *
     * @param msf one multi value solr field
     * @param env contains values which are needed when the field is mapped
     * @return false to stop visiting otherwise true
     */
    public boolean visitField(SolrMultiValuedField msf, ScriptEnv env);
}
