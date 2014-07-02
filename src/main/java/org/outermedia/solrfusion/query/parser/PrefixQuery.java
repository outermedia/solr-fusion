package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;

@ToString(callSuper = true)
public class PrefixQuery extends TermQuery
{

	public PrefixQuery(Term prefix)
	{
		super(prefix);
	}

}
