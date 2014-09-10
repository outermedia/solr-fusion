package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * A double range query.
 *
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class DoubleRangeQuery extends NumericRangeQuery<Double>
{

    public DoubleRangeQuery(String field, Double min, Double max, boolean minInclusive, boolean maxInclusive)
    {
        super(field, minInclusive, maxInclusive, min, max);
    }

    protected DoubleRangeQuery()
    {
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    @Override
    public DoubleRangeQuery shallowClone()
    {
        return shallowCloneImpl(new DoubleRangeQuery());
    }
}
