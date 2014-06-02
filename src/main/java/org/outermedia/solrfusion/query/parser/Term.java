package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.configuration.FusionField;

@ToString
@Getter
@Setter
public class Term
{
	private String field;
	private String termStr;
	private FusionField fusionField;

	public Term(String field, String termStr)
	{
		this.field = field;
		this.termStr = termStr;
	}

	public String field()
	{
		return field;
	}

}
