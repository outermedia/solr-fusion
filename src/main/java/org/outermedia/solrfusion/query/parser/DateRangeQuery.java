package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Calendar;

/**
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class DateRangeQuery extends NumericRangeQuery<Calendar>
{

    public DateRangeQuery(String field, Calendar min, Calendar max, boolean minInclusive, boolean maxInclusive)
    {
        super(field, minInclusive, maxInclusive, min, max);
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
