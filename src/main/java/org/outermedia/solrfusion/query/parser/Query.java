package org.outermedia.solrfusion.query.parser;

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
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.VisitableQuery;

/**
 * The abstract super class of all Solr query objects.
 */
@ToString
@Getter
@Setter
public abstract class Query implements VisitableQuery
{
    private Float boostValue;

    // when new queries are added (om:add)
    // otherwise outside
    private Boolean addInside;

    private MetaInfo metaInfo;

    public void setBoost(float f)
    {
        this.boostValue = f;
    }

    public boolean isNewQuery()
    {
        return addInside != null;
    }

    public boolean isInside()
    {
        return addInside != null && addInside;
    }

    public boolean isOutside()
    {
        return addInside != null && addInside;
    }

    public void resetQuery()
    {
        if (metaInfo != null)
        {
            metaInfo.resetQuery();
        }
    }

    public boolean isDismaxQuery()
    {
        return metaInfo != null && metaInfo.isDismax();
    }
}
