package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * An int range query.
 *
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class IntRangeQuery extends NumericRangeQuery<Integer>
{

    public IntRangeQuery(String field, Integer min, Integer max, boolean minInclusive, boolean maxInclusive)
    {
        super(field, minInclusive, maxInclusive, min, max);
    }

    protected IntRangeQuery()
    {
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    @Override
    public IntRangeQuery shallowClone()
    {
        return shallowCloneImpl(new IntRangeQuery());
    }
}
