package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class LongRangeQuery extends NumericRangeQuery
{
    private final Long min;
    private final Long max;

    public LongRangeQuery(String field, Long min, Long max, boolean minInclusive, boolean maxInclusive)
    {
        super(field, minInclusive, maxInclusive);
        this.min = min;
        this.max = max;
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }
}
