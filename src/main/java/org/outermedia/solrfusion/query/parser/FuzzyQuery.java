package org.outermedia.solrfusion.query.parser;

import lombok.ToString;

@ToString(callSuper = true)
public class FuzzyQuery extends TermQuery
{

	public FuzzyQuery(Term term, int maxEdits, int prefixLength)
	{
		super(term);
		// TODO Auto-generated constructor stub
	}

}
