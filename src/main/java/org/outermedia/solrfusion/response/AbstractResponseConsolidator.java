package org.outermedia.solrfusion.response;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract super class of ResponseConsolidatorIfc implementations.
 *
 * Created by ballmann on 7/9/14.
 */
public abstract class AbstractResponseConsolidator implements ResponseConsolidatorIfc
{
    protected List<Exception> errorResponses;

    public AbstractResponseConsolidator()
    {
        errorResponses = new ArrayList<>();
    }

    @Override public void addErrorResponse(Exception se)
    {
        errorResponses.add(se);
    }

    public String getErrorMsg()
    {
        StringBuilder sb = new StringBuilder();
        if(errorResponses != null)
        {
            for(Exception ex : errorResponses)
            {
                sb.append(ex.getMessage());
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
