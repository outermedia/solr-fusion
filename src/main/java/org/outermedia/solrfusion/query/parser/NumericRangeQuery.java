package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Calendar;

@Getter
@ToString(callSuper = true)
public abstract class NumericRangeQuery<T> extends Query
{
    private Term min;
    private Term max;
    private boolean minInclusive;
    private boolean maxInclusive;


    @Override
    public abstract void accept(QueryVisitor visitor, ScriptEnv env);

    protected NumericRangeQuery(String field, boolean minInclusive, boolean maxInclusive, T min, T max)
    {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.min = Term.newFusionTerm(field,limitValueAsString(min));
        this.max = Term.newFusionTerm(field,limitValueAsString(max));
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

    protected String limitValueAsString(T v)
    {
        String result = "*";
        if(v != null)
        {
            result = v.toString();
        }
        return result;
    }
}
