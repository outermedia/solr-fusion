package org.outermedia.solrfusion.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;

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
    private List<Query> newTerms;

    private Term()
    {
        removed = false;
        wasMapped = false;
    }

    public static Term newFusionTerm(String field, String termStr)
    {
        Term result = new Term();
        result.fusionFieldName = field;
        result.fusionFieldValue = termStr;
        return result;
    }

    public static Term newSearchServerTerm(String field, String termStr)
    {
        Term result = new Term();
        result.searchServerFieldName = field;
        result.searchServerFieldValue = termStr;
        return result;
    }

    public String field()
    {
        return fusionFieldName;
    }

    public void addNewSearchServerTerm(String searchServersFieldName, String searchServersFieldValue)
    {
        if (newTerms == null)
        {
            newTerms = new ArrayList<>();
        }
        newTerms.add(new TermQuery(newSearchServerTerm(searchServersFieldName, searchServersFieldValue)));
    }

    public void addNewFusionTerm(String fusionFieldName, String fusionFieldValue)
    {
        if (newTerms == null)
        {
            newTerms = new ArrayList<>();
        }
        newTerms.add(new TermQuery(newFusionTerm(fusionFieldName, fusionFieldValue)));
    }
}
