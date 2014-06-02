package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
@Setter
public class TermQuery extends Query
{
	private Term term;

	public TermQuery(Term term)
	{
		this.term = term;
	}
}
