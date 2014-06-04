package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.VisitableQuery;

@ToString
@Getter
@Setter
public abstract class Query implements VisitableQuery
{

    public void setBoost(float f)
    {
        // TODO Auto-generated method stub

    }

}
