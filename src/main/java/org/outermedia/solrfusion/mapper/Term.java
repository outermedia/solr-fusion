package org.outermedia.solrfusion.mapper;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Data holder which stores the values of one Solr field and the corresponding SolrFusion field.
 *
 * @author sballmann
 */

@ToString(exclude = {"fusionFacetCount", "searchServerFacetCount"})
@Getter
@Setter
@Slf4j
public class Term
{
    private String fusionFieldName;
    private List<String> fusionFieldValue;
    private FusionField fusionField;
    private List<Integer> fusionFacetCount;

    private String searchServerFieldName;
    private List<String> searchServerFieldValue;
    private List<Integer> searchServerFacetCount;

    // set by an remove operation
    private boolean removed;

    // set by a change operation
    private boolean wasMapped;

    private boolean processed;

    // added by an add operation
    private List<String> newQueries;


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
        if (termStr != null)
        {
            result.fusionFieldValue.addAll(termStr);
        }
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

    public void resetQuery()
    {
        searchServerFieldName = null;
        searchServerFieldValue = null;
        searchServerFacetCount = null;
        removed = false;
        wasMapped = false;
        processed = false;
        newQueries = null;
    }

    public void resetSearchServerField()
    {
        fusionFieldName = null;
        fusionFieldValue = null;
        fusionFacetCount = null;
        fusionField = null;
        removed = false;
        wasMapped = false;
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

    /**
     * Add a new query part to a search server's query.
     *
     * @param inside                 if true or outside (false)
     * @param searchServerFieldValue one complete search server query
     */
    public void addNewSearchServerQuery(boolean inside, List<String> searchServerFieldValue,
        Configuration configuration, Locale locale)
    {
        if (newQueries == null)
        {
            newQueries = new ArrayList<>();
        }
        // TODO throw exception if searchServerFieldValue.size() == 0
        for (String qs : searchServerFieldValue)
        {
            // qs contains whole query, otherwise it would be difficult to mix-in the field name, because complex
            // queries are possible here
            newQueries.add(qs);
        }
        setWasMapped(true);
    }

    public String mergeFusionValues()
    {
        String result = null;
        if (fusionFieldValue != null)
        {
            result = Joiner.on(",").join(fusionFieldValue);
        }
        return result;
    }

    public String mergeSearchServerValues()
    {
        String result = null;
        if (searchServerFieldValue != null)
        {
            result = Joiner.on(",").join(searchServerFieldValue);
        }
        return result;
    }

    public Term shallowClone()
    {
        Term newTerm = new Term();
        newTerm.fusionFieldName = fusionFieldName;
        newTerm.fusionFieldValue = fusionFieldValue;
        newTerm.fusionField = fusionField;
        newTerm.fusionFacetCount = fusionFacetCount;
        newTerm.searchServerFieldName = searchServerFieldName;
        newTerm.searchServerFieldValue = searchServerFieldValue;
        newTerm.searchServerFacetCount = searchServerFacetCount;
        newTerm.removed = removed;
        newTerm.wasMapped = wasMapped;
        newTerm.processed = processed;
        newTerm.newQueries = null;
        return newTerm;
    }
}
