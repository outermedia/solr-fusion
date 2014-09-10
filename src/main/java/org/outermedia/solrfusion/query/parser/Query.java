package org.outermedia.solrfusion.query.parser;

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
