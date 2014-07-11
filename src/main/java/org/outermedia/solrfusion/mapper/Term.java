package org.outermedia.solrfusion.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.outermedia.solrfusion.response.parser.SolrField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> fusionFieldValue;
    private FusionField fusionField;
    private String searchServerFieldName;
    private List<String> searchServerFieldValue;

    // set by an remove operation
    private boolean removed;

    // set by a change operation
    private boolean wasMapped;

    private boolean processed;

    // added by an add operation
    private List<Query> newQueryTerms;
    // added by an add operation
    private List<SolrField> newResponseValues;

    private Term()
    {
        removed = false;
        wasMapped = false;
        processed = false;
    }

    public static Term newFusionTerm(String field, String... termStr)
    {
        return newFusionTerm(field, Arrays.asList(termStr));
    }

    public static Term newFusionTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.fusionFieldName = field;
        result.fusionFieldValue = new ArrayList<>();
        result.fusionFieldValue.addAll(termStr);
        return result;
    }

    public static Term newSearchServerTerm(String field, String... termStr)
    {
        return newSearchServerTerm(field, Arrays.asList(termStr));
    }

    public static Term newSearchServerTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.searchServerFieldName = field;
        result.searchServerFieldValue = new ArrayList<>();
        result.searchServerFieldValue.addAll(termStr);
        return result;
    }

    public String field()
    {
        return fusionFieldName;
    }

    public void addNewSearchServerTerm(String searchServersFieldName, List<String> searchServersFieldValue)
    {
        if (newResponseValues == null)
        {
            newResponseValues = new ArrayList<>();
        }
        SolrSingleValuedField responseField = new SolrSingleValuedField();
        // TODO maybe already mapped value (newFusionTerm() instead of newSearchServerTerm()) ?!
        // TODO maybe SolrMultiValueField instead of SolrSingleValuedField?!
        responseField.setTerm(newSearchServerTerm(searchServersFieldName, searchServersFieldValue));
        newResponseValues.add(responseField);
    }

    public void addNewFusionTerm(String fusionFieldName, List<String> fusionFieldValue)
    {
        if (newQueryTerms == null)
        {
            newQueryTerms = new ArrayList<>();
        }
        newQueryTerms.add(new TermQuery(newFusionTerm(fusionFieldName, fusionFieldValue)));
    }

    public void resetQuery()
    {
        searchServerFieldName = null;
        searchServerFieldValue = null;
        removed = false;
        wasMapped = false;
        newQueryTerms = null;
        processed = false;
    }

    public void resetSearchServerField()
    {
        fusionFieldName = null;
        fusionFieldValue = null;
        fusionField = null;
        removed = false;
        wasMapped = false;
        newResponseValues = null;
        processed = false;
    }

    public int compareFusionValue(Term t)
    {
        // sort the uncomparable to one end
        int unknownReturnValue = -1;
        if (t == null)
        {
            return unknownReturnValue;
        }
        List<String> thisFusionValues = getFusionFieldValue();
        List<String> otherFusionValues = t.getFusionFieldValue();
        if (thisFusionValues == null && otherFusionValues == null)
        {
            return 0;
        }
        if (thisFusionValues == null)
        {
            return -unknownReturnValue;
        }
        if (otherFusionValues == null)
        {
            return unknownReturnValue;
        }
        int thisSize = thisFusionValues.size();
        int otherSize = otherFusionValues.size();
        if (thisSize >= 1)
        {
            if (otherSize == 0)
            {
                return unknownReturnValue;
            }
            else
            {
                // for multi values only the first field is used!
                String otherValue = otherFusionValues.get(0);
                String thisValue = thisFusionValues.get(0);
                return thisValue.compareTo(otherValue);
            }
        }
        else
        {
            if (otherSize == 0)
            {
                return 0;
            }
            return -unknownReturnValue;
        }
    }

}
