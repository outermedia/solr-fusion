package org.outermedia.solrfusion.query.parser;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

@ToString(callSuper = true)
public class BooleanQuery extends Query
{
	private List<BooleanClause> clauses;

	public BooleanQuery(boolean disableCoord)
	{
		clauses = new ArrayList<>();
		// TODO Auto-generated constructor stub 
	}

	public void add(BooleanClause clause)
	{
		clauses.add(clause);
	}

}
