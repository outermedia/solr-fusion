package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class represents a simple term query aka "&lt;field&gt;:&lt;value&gt;".
 *
 * @author ballmann
 */

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
