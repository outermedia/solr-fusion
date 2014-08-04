package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ballmann on 6/27/14.
 */
@ToString(callSuper = true)
public class DateRangeQuery extends NumericRangeQuery<Calendar>
{
    // see lucene's DateTools, but we omit HH (no hours)
    protected static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public DateRangeQuery(String field, Calendar min, Calendar max, boolean minInclusive, boolean maxInclusive)
    {
        super(field, minInclusive, maxInclusive, min, max);
    }

    protected DateRangeQuery()
    {
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    @Override protected String limitValueAsString(Calendar v)
    {
        String result = "*";
        if(v != null)
        {
            // see lucene's DateTools
            format.setTimeZone(v.getTimeZone());
            result = format.format(v.getTime());
        }
        return result;
    }

    @Override
    public DateRangeQuery shallowClone()
    {
        return shallowCloneImpl(new DateRangeQuery());
    }
}
