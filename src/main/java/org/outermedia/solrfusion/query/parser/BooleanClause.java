package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BooleanClause
{
	private int occur = Occur_MAY; // TODO correct initialization?
	private Query q;

	public static final int Occur_MAY = 0;

	public static final int Occur_MUST = 1;

	public static final int Occur_SHOULD = 2;

	public static final int Occur_MUST_NOT = 3;

	public BooleanClause(Query q, int occur)
	{
		this.q = q;
		this.occur = occur;
	}

	public boolean isProhibited()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
