package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Calendar;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class NumericRangeQuery extends Query
{
    private String fusionFieldName;
    private boolean minInclusive;
    private boolean maxInclusive;

    @Override
    public abstract void accept(QueryVisitor visitor, ScriptEnv env);

    protected NumericRangeQuery(String field, boolean minInclusive, boolean maxInclusive)
    {
        this.fusionFieldName = field;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

	public static NumericRangeQuery newLongRange(String field, Long min,
		Long max, boolean minInclusive, boolean maxInclusive)
	{
		return new LongRangeQuery(field, min, max, minInclusive, maxInclusive);
	}

	public static NumericRangeQuery newIntRange(String field, Integer min,
		Integer max, boolean minInclusive, boolean maxInclusive)
	{
		return new IntRangeQuery(field, min, max, minInclusive, maxInclusive);
	}

	public static NumericRangeQuery newFloatRange(String field,
		Float min, Float max, boolean minInclusive, boolean maxInclusive)
	{
        return new FloatRangeQuery(field, min, max, minInclusive, maxInclusive);
	}

	public static NumericRangeQuery newDoubleRange(String field,
		Double min, Double max, boolean minInclusive, boolean maxInclusive)
	{
		return new DoubleRangeQuery(field, min, max, minInclusive, maxInclusive);
	}

    /**
     *
     * @param field
     * @param min either null ("*") or an instance of GregorianCalendar
     * @param max either null ("*") or an instance of GregorianCalendar
     * @param minInclusive
     * @param maxInclusive
     * @return
     */
    public static NumericRangeQuery newDateRange(String field,
            Calendar min, Calendar max, boolean minInclusive, boolean maxInclusive)
    {
        return new DateRangeQuery(field, min, max, minInclusive, maxInclusive);
    }
}
