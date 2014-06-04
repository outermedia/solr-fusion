package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
public class NumericRangeQuery extends Query
{
    private String fusionFieldName;

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

	public static NumericRangeQuery newLongRange(String field, Long min,
		Long max, boolean inclusive, boolean inclusive2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static NumericRangeQuery newIntRange(String field, Integer min,
		Integer max, boolean inclusive, boolean inclusive2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static NumericRangeQuery newFloatRange(String field, int i,
		Float min, Float max, boolean inclusive, boolean inclusive2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static NumericRangeQuery newDoubleRange(String field, int i,
		Double min, Double max, boolean inclusive, boolean inclusive2)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
