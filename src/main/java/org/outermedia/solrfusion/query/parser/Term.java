package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.configuration.FusionField;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder which stores a fusion field name and its value.
 *
 * @author sballmann
 */

@ToString
@Getter
@Setter
public class Term
{
	private String fusionFieldName;
	private String fusionFieldValue;
	private FusionField fusionField;
    private String searchServerFieldName;
    private String searchServerFieldValue;

    // set by an remove operation
    private boolean removed;

    // set by a change operation
    private boolean wasMapped;

    // added by an add operation
    private List<Query> newQueries;


    public Term(String field, String termStr)
	{
		this.fusionFieldName = field;
		this.fusionFieldValue = termStr;
        removed = false;
        wasMapped = false;
	}

	public String field()
	{
		return fusionFieldName;
	}

    public void addNewSearchServerQuery(String searchServersFieldName, String searchServersFieldValue)
    {
        if (newQueries == null)
        {
            newQueries = new ArrayList<>();
        }
        newQueries.add(new TermQuery(new Term(searchServersFieldName, searchServersFieldValue)));
    }
}
