package org.outermedia.solrfusion.query.parser;

import lombok.ToString;

@ToString(callSuper = true)
public class PrefixQuery extends TermQuery
{

	public PrefixQuery(Term prefix)
	{
		super(prefix);
		// TODO Auto-generated constructor stub
	}
}
