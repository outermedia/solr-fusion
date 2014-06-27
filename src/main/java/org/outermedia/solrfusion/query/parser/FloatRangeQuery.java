package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class FloatRangeQuery extends NumericRangeQuery
{
    private final Float min;
    private final Float max;

    public FloatRangeQuery(String field, Float min, Float max, boolean minInclusive, boolean maxInclusive)
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
