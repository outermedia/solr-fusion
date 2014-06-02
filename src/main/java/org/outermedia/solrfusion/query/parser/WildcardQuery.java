package org.outermedia.solrfusion.query.parser;

import lombok.ToString;

@ToString(callSuper = true)
public class WildcardQuery extends TermQuery
{

	public WildcardQuery(Term t)
	{
		super(t);
		// TODO Auto-generated constructor stub
	}

}
