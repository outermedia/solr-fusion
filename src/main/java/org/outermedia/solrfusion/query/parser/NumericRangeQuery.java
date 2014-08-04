package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Calendar;

/**
 * Subclasses have to implement the shallowClone() and the accept() method.
 *
 * @param <T>
 */
@ToString(callSuper = true)
public abstract class NumericRangeQuery<T> extends TermQuery
{
    @Override
    public abstract void accept(QueryVisitor visitor, ScriptEnv env);

    /**
     * Create a new range query.
     *
     * @param field
     * @param minInclusive is ignored
     * @param maxInclusive is ignored
     * @param min
     * @param max
     */
    protected NumericRangeQuery(String field, boolean minInclusive, boolean maxInclusive, T min, T max)
    {
        setTerm(Term.newFusionTerm(field, limitValueAsString(min), limitValueAsString(max)));
    }

    protected NumericRangeQuery()
    {
    }

    public static NumericRangeQuery newLongRange(String field, Long min, Long max, boolean minInclusive,
        boolean maxInclusive)
    {
        return new LongRangeQuery(field, min, max, minInclusive, maxInclusive);
    }

    public static NumericRangeQuery newIntRange(String field, Integer min, Integer max, boolean minInclusive,
        boolean maxInclusive)
    {
        return new IntRangeQuery(field, min, max, minInclusive, maxInclusive);
    }

    public static NumericRangeQuery newFloatRange(String field, Float min, Float max, boolean minInclusive,
        boolean maxInclusive)
    {
        return new FloatRangeQuery(field, min, max, minInclusive, maxInclusive);
    }

    public static NumericRangeQuery newDoubleRange(String field, Double min, Double max, boolean minInclusive,
        boolean maxInclusive)
    {
        return new DoubleRangeQuery(field, min, max, minInclusive, maxInclusive);
    }

    /**
     * @param field
     * @param min          either null ("*") or an instance of GregorianCalendar
     * @param max          either null ("*") or an instance of GregorianCalendar
     * @param minInclusive
     * @param maxInclusive
     * @return
     */
    public static NumericRangeQuery newDateRange(String field, Calendar min, Calendar max, boolean minInclusive,
        boolean maxInclusive)
    {
        return new DateRangeQuery(field, min, max, minInclusive, maxInclusive);
    }

    protected String limitValueAsString(T v)
    {
        String result = "*";
        if (v != null)
        {
            result = v.toString();
        }
        return result;
    }

    public String getFusionFieldName()
    {
        return getTerm().getFusionFieldName();
    }

    public String getSearchServerFieldName()
    {
        return getTerm().getSearchServerFieldName();
    }

    public String getMinFusionValue()
    {
        return getTerm().getFusionFieldValue().get(0);
    }

    public String getMinSearchServerValue()
    {
        return getTerm().getSearchServerFieldValue().get(0);
    }

    public String getMaxFusionValue()
    {
        return getTerm().getFusionFieldValue().get(1);
    }

    public String getMaxSearchServerValue()
    {
        return getTerm().getSearchServerFieldValue().get(1);
    }

    public boolean isRemoved() { return getTerm().isRemoved(); }

    public boolean isWasMapped() { return getTerm().isWasMapped(); }
}
