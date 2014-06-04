package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;

@ToString(callSuper = true)
public class WildcardQuery extends TermQuery
{

	public WildcardQuery(Term t)
	{
		super(t);
		// TODO Auto-generated constructor stub
	}

}
